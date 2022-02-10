package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

internal abstract class AbstractQueryImpl<T: Immutable>(
    val tableAliasAllocator: TableAliasAllocator,
    val entityTypeMap: Map<KClass<out Immutable>, EntityType>,
    type: KClass<T>
): AbstractSqlQuery<T>, Renderable {

    private val predicates = mutableListOf<Expression<Boolean>>()

    private val groupByExpressions = mutableListOf<Expression<*>>()

    private val havingPredicates = mutableListOf<Expression<Boolean>>()

    private val orders = mutableListOf<Order>()

    override val table: TableImpl<T> = TableImpl(
        this.let { it }, // Boring code ".let{ it }" is used to avoid compilation warning
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

    override fun <X : Immutable> subQuery(
        type: KClass<X>,
        block: (SqlSubQuery<T, X>.() -> Unit)?
    ): SqlSubQuery<T, X> =
        SubQueryImpl(this, type).apply {
            block?.invoke(this)
        }

    override fun <X : Immutable, R> typedSubQuery(
        type: KClass<X>,
        block: SqlSubQuery<T, X>.() -> TypedSqlSubQuery<T, X, R>
    ): TypedSqlSubQuery<T, X, R> =
        SubQueryImpl(this, type).run {
            block?.invoke(this)
        }

    override fun renderTo(builder: SqlBuilder) {
        renderWithoutSelection(builder)
    }

    fun renderWithoutSelection(builder: SqlBuilder) {
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
            if (orders.isNotEmpty()) {
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
}