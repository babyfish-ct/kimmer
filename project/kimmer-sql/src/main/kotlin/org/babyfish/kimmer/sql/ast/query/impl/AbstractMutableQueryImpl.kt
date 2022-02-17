package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Order
import org.babyfish.kimmer.sql.spi.Renderable
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.MutableQuery
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.ast.AstVisitor
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal abstract class AbstractMutableQueryImpl<E, ID>(
    val tableAliasAllocator: TableAliasAllocator,
    val sqlClient: SqlClientImpl,
    type: KClass<E>
): MutableQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    private val entityTypeMap: Map<KClass<out Immutable>, EntityType>
        get() = sqlClient.entityTypeMap

    private val predicates = mutableListOf<NonNullExpression<Boolean>>()

    private val groupByExpressions = mutableListOf<Expression<*>>()

    private val havingPredicates = mutableListOf<NonNullExpression<Boolean>>()

    private val orders = mutableListOf<Order>()

    private var frozen = false

    override val table: TableImpl<E, ID> = createTable0(type)

    private fun createTable0(type: KClass<E>): TableImpl<E, ID> =
        createTable(
            entityTypeMap[type]
                ?: throw IllegalArgumentException("Cannot create query for unmapped type '${type.qualifiedName}'")
        )

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

    override fun <X, XID, R: Any> subQuery(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> TypedSubQuery<E, ID, X, XID, R>
    ): TypedSubQuery<E, ID, X, XID, R>
    where X: Entity<XID>, XID: Comparable<XID> =
        SubMutableQueryImpl(this, type).run {
            block()
        }

    override fun <X, XID> untypedSubQuery(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> Unit
    ): MutableSubQuery<E, ID, X, XID>
    where X: Entity<XID>, XID: Comparable<XID> =
        SubMutableQueryImpl(this, type).apply {
            block()
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
                overriddenSelections.forEach { it.accept(this) }
            }
        }
    }

    private class UseJoinOfIgnoredClauseVisitor(
        override val sqlBuilder: AbstractSqlBuilder
    ): AstVisitor {

        override fun visitSubQuery(
            subQuery: TypedSubQuery<*, *, *, *, *>
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

    fun freeze() {
        frozen = true
    }

    fun validateMutable() {
        if (frozen) {
            throw IllegalStateException("Cannot mutate the query because it has been frozen")
        }
    }
}