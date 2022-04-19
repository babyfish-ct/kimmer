package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Order
import org.babyfish.kimmer.sql.spi.Renderable
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.ast.AstVisitor
import org.babyfish.kimmer.sql.ast.query.*
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.impl.AbstractMutableStatement
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal abstract class AbstractMutableQueryImpl<E, ID> private constructor(
    tableAliasAllocator: TableAliasAllocator,
    sqlClient: SqlClientImpl
): AbstractMutableStatement(
    tableAliasAllocator,
    sqlClient
), MutableQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    internal val predicates = mutableListOf<NonNullExpression<Boolean>>()

    private val groupByExpressions = mutableListOf<Expression<*>>()

    private val havingPredicates = mutableListOf<NonNullExpression<Boolean>>()

    private val orders = mutableListOf<Order>()

    private var frozen = false

    private lateinit var _table: TableImpl<E, ID>

    private lateinit var _subQueries: SubQueries<E, ID>

    private lateinit var _wildSubQueries: WildSubQueries<E, ID>

    override val table: TableImpl<E, ID>
        get() = _table

    constructor(
        tableAliasAllocator: TableAliasAllocator,
        sqlClient: SqlClientImpl,
        type: KClass<E>
    ): this(
        tableAliasAllocator,
        sqlClient
    ) {
        val entityType = sqlClient.entityTypeMap[type]
            ?: throw IllegalArgumentException("Cannot create query base on unmapped type '$type'")
        _table = createTable(entityType)
    }

    constructor(
        tableAliasAllocator: TableAliasAllocator,
        sqlClient: SqlClientImpl,
        prop: KProperty1<*, *>
    ): this(
        tableAliasAllocator,
        sqlClient
    ) {
        val ownerType = prop.parameters[0].type.classifier as KClass<*>?
            ?: throw IllegalArgumentException(
                "Cannot create association query because cannot extract owner type from '$prop'"
            )
        val ownerEntityType = sqlClient.entityTypeMap[ownerType]
            ?: throw IllegalArgumentException(
                "Cannot create association query base on unmapped type '$ownerType'"
            )
        val entityProp = ownerEntityType.props[prop.name]
            ?: throw IllegalArgumentException(
                "Cannot create association query because there is no entity property '${prop.name}' in the type '${ownerEntityType}'"
            )
        val associationEntityType = (entityProp as EntityPropImpl).associationEntityType
            ?: throw IllegalArgumentException(
                "Cannot create association query because '$prop' is not base on middle table"
            )
        _table = createTable(associationEntityType)
    }

    protected open fun createTable(entityType: EntityType): TableImpl<E, ID> =
        TableImpl(this, entityType)

    override fun where(vararg predicates: NonNullExpression<Boolean>?) {

        validateMutable()

        for (predicate in predicates) {
            predicate?.let {
                this.predicates += it
            }
        }
    }

    override fun where(block: () -> NonNullExpression<Boolean>?) {

        validateMutable()

        block()?.let {
            predicates += it
        }
    }

    override fun groupBy(vararg expression: Expression<*>) {

        validateMutable()

        groupByExpressions += expression
    }

    override fun having(vararg predicates: NonNullExpression<Boolean>?) {

        validateMutable()

        for (predicate in predicates) {
            predicate?.let {
                this.havingPredicates += it
            }
        }
    }

    override fun having(block: () -> NonNullExpression<Boolean>?) {

        validateMutable()

        block()?.let {
            havingPredicates += it
        }
    }

    override fun orderBy(
        expression: Expression<*>?,
        mode: OrderMode,
        nullMode: NullOrderMode
    ) {

        validateMutable()

        expression?.let {
            orders += Order(expression, mode, nullMode)
        }
    }

    override fun clearWhereClauses() {
        validateMutable()
        predicates.clear()
    }

    override fun clearGroupByClauses() {
        validateMutable()
        groupByExpressions.clear()
    }

    override fun clearHavingClauses() {
        validateMutable()
        havingPredicates.clear()
    }

    override fun clearOrderByClauses() {
        validateMutable()
        orders.clear()
    }

    override val subQueries: SubQueries<E, ID> by lazy {
        SubQueriesImpl(this)
    }

    override val wildSubQueries: WildSubQueries<E, ID> by lazy {
        WildSubQueriesImpl(this)
    }

    fun renderTo(builder: SqlBuilder, withoutSortingAndPaging: Boolean) {
        (table as Renderable).renderTo(builder)
        builder.apply {
            if (predicates.isNotEmpty()) {
                sql(" where ")
                var separator: String? = null
                for (predicate in predicates) {
                    if (separator === null) {
                        separator = " and "
                    } else {
                        sql(separator)
                    }
                    (predicate as Renderable).renderTo(this)
                }
            }
            if (groupByExpressions.isNotEmpty()) {
                sql(" group by ")
                var separator: String? = null
                for (expression in groupByExpressions) {
                    if (separator === null) {
                        separator = ", "
                    } else {
                        sql(separator)
                    }
                    (expression as Renderable).renderTo(this)
                }
            }
            if (havingPredicates.isNotEmpty()) {
                sql(" having ")
                var separator: String? = null
                for (predicate in havingPredicates) {
                    if (separator === null) {
                        separator = " and "
                    } else {
                        sql(separator)
                    }
                    (predicate as Renderable).renderTo(this)
                }
            }
            if (!withoutSortingAndPaging && orders.isNotEmpty()) {
                sql(" order by ")
                var separator: String? = null
                for (order in orders) {
                    if (separator === null) {
                        separator = ", "
                    } else {
                        sql(separator)
                    }
                    (order as Renderable).renderTo(this)
                }
            }
        }
    }

    fun accept(
        visitor: AstVisitor,
        overriddenSelections: List<Selection<*>>?,
        withoutSortingAndPaging: Boolean
    ) {
        if (groupByExpressions.isEmpty() && havingPredicates.isNotEmpty()) {
            throw IllegalStateException(
                "Having clause cannot be used without group clause"
            )
        }
        val sqlBuilder = (visitor.sqlBuilder as AbstractSqlBuilder?)
            ?: error("Internal bug: AbstractQueryImpl.accept can only be used during sql rendering")
        predicates.forEach { it.accept(visitor) }
        groupByExpressions.forEach { it.accept(visitor) }
        havingPredicates.forEach { it.accept(visitor) }
        if (withoutSortingAndPaging) {
            UseJoinOfIgnoredClauseVisitor(sqlBuilder).apply {
                orders.forEach { it.accept(this) }
            }
        } else {
            orders.forEach { it.accept(visitor) }
        }
        if (overriddenSelections !== null) {
            UseJoinOfIgnoredClauseVisitor(sqlBuilder).apply {
                overriddenSelections.forEach { (it as Ast).accept(this) }
            }
        }
    }

    private class UseJoinOfIgnoredClauseVisitor(
        override val sqlBuilder: AbstractSqlBuilder
    ): AstVisitor {

        override fun visitSubQuery(
            subQuery: TypedSubQuery<*>
        ): Boolean = false

        override fun visitTableReference(table: Table<*, *>, prop: EntityProp?) {
            handle(table as TableImpl<*, *>, prop?.isId ?: false)
        }

        private fun handle(table: TableImpl<*, *>, isId: Boolean) {
            if (table.destructive !== TableImpl.Destructive.NONE) {
                if (isId) {
                    sqlBuilder.useTable(table.parent!!)
                } else {
                    sqlBuilder.useTable(table)
                }
                return
            }
            table.parent?.let {
                handle(it, false)
            }
        }
    }

    fun isGroupByClauseUsed(): Boolean =
        groupByExpressions.isNotEmpty()
}