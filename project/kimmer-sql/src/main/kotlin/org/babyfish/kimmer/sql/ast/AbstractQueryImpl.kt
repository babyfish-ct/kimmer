package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

internal abstract class AbstractQueryImpl<E, ID>(
    val tableAliasAllocator: TableAliasAllocator,
    val sqlClient: SqlClientImpl,
    type: KClass<E>
): AbstractSqlQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    protected val entityTypeMap: Map<KClass<out Immutable>, EntityType>
        get() = sqlClient.entityTypeMap

    private val predicates = mutableListOf<Expression<Boolean>>()

    private val groupByExpressions = mutableListOf<Expression<*>>()

    private val havingPredicates = mutableListOf<Expression<Boolean>>()

    private val orders = mutableListOf<Order>()

    override val table: TableImpl<E, ID> = createTable(type)

    protected open fun createTable(type: KClass<E>): TableImpl<E, ID> =
        TableImpl(
            this,
            entityTypeMap[type]
                ?: throw IllegalArgumentException("Cannot create query for unmapped type '${type.qualifiedName}'")
        )

    override fun where(vararg predicates: Expression<Boolean>?) {
        for (predicate in predicates) {
            predicate?.let {
                this.predicates += it
            }
        }
    }

    override fun groupBy(vararg expression: Expression<*>) {
        groupByExpressions += expression
    }

    override fun having(vararg predicates: Expression<Boolean>?) {
        for (predicate in predicates) {
            predicate?.let {
                this.havingPredicates += it
            }
        }
    }

    override fun orderBy(expression: Expression<*>?, descending: Boolean) {
        expression?.let {
            orders += Order(expression, descending)
        }
    }

    override fun clearWhereClauses() {
        predicates.clear()
    }

    override fun clearGroupByClauses() {
        groupByExpressions.clear()
    }

    override fun clearHavingClauses() {
        havingPredicates.clear()
    }

    override fun clearOrderByClauses() {
        orders.clear()
    }

    override fun <X, XID, R> subQuery(
        type: KClass<X>,
        block: SqlSubQuery<E, ID, X, XID>.() -> TypedSqlSubQuery<E, ID, X, XID, R>
    ): TypedSqlSubQuery<E, ID, X, XID, R>
    where X: Entity<XID>, XID: Comparable<XID> =
        SubQueryImpl(this, type).run {
            block()
        }

    override fun <X, XID> untypedSubQuery(
        type: KClass<X>,
        block: SqlSubQuery<E, ID, X, XID>.() -> Unit
    ): SqlSubQuery<E, ID, X, XID>
    where X: Entity<XID>, XID: Comparable<XID> =
        SubQueryImpl(this, type).apply {
            block()
        }

    fun renderTo(builder: SqlBuilder, withoutSortingAndPaging: Boolean = false) {
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

    fun accept(visitor: TableReferenceVisitor, withoutSortingAndPaging: Boolean = false) {
        predicates.forEach { it.accept(visitor) }
        groupByExpressions.forEach { it.accept(visitor) }
        havingPredicates.forEach { it.accept(visitor) }
        if (withoutSortingAndPaging) {
            UseJoinOfIgnoredOrderClauseVisitor(visitor.sqlBuilder).apply {
                orders.forEach { it.accept(this) }
            }
        } else {
            orders.forEach { it.accept(visitor) }
        }
    }

    private class UseJoinOfIgnoredOrderClauseVisitor(
        override val sqlBuilder: SqlBuilder
    ): TableReferenceVisitor {

        override fun skipSubQuery(): Boolean = true

        override fun visit(table: TableImpl<*, *>, entityProp: EntityProp?) {
            entityProp ?: error(
                "Internal bug: star selection cannot be " +
                    "visited because sub query is skipped"
            )
            handle(table, entityProp.isId)
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
}