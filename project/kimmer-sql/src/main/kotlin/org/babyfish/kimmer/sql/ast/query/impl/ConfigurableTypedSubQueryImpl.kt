package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedSubQuery
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor

internal class ConfigurableTypedSubQueryImpl<P, PID, E, ID, R>(
    data: TypedQueryData,
    baseQuery: SubQueryImpl<P, PID, E, ID>
): AbstractTypedQueryImpl<E, ID, R>(
    data,
    baseQuery
), ConfigurableTypedSubQuery<P, PID, E, ID, R>,
    TypedSubQueryImplementor<P, PID, E, ID>, 
    Renderable, 
    TableReferenceElement
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID>,
        R: Any {

    private val _selectedType: Class<R>?

    init {
        _selectedType = data
            .selections
            .takeIf { it.size == 1 }
            ?.filterIsInstance<Expression<R>>()
            ?.firstOrNull { it.isSelectable }
            ?.selectedType
    }

    override val isSelectable: Boolean
        get() = _selectedType !== null

    override val selectedType: Class<R>
        get() = _selectedType ?: error("The current sub query cannot be selected directly")

    @Suppress("UNCHECKED_CAST")
    override val baseQuery: SubQueryImpl<P, PID, E, ID>
        get() = super.baseQuery as SubQueryImpl<P, PID, E, ID>

    override fun limit(limit: Int, offset: Int): ConfigurableTypedSubQuery<P, PID, E, ID, R> =
        if (data.limit == limit && data.offset == offset) {
            this
        } else {
            if (limit < 0) {
                throw IllegalArgumentException("'limit' can not be less than 0")
            }
            if (offset < 0) {
                throw IllegalArgumentException("'offset' can not be less than 0")
            }
            if (limit > Int.MAX_VALUE - offset) {
                throw IllegalArgumentException("'limit' > Int.MAX_VALUE - offset")
            }
            ConfigurableTypedSubQueryImpl(
                data = data.copy(limit = limit, offset = offset),
                baseQuery = baseQuery
            )
        }

    override fun distinct(distinct: Boolean): ConfigurableTypedSubQuery<P, PID, E, ID, R> =
        if (data.distinct === distinct) {
            this
        } else {
            ConfigurableTypedSubQueryImpl(
                data.copy(distinct = distinct),
                baseQuery
            )
        }

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("(")
        super.renderTo(builder)
        builder.sql(")")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        if (!visitor.skipSubQuery()) {
            super.accept(visitor)
        }
    }

    companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <P: Entity<PID>, PID: Comparable<PID>, E: Entity<ID>, ID: Comparable<ID>, R: Any> select(
            query: SubQueryImpl<P, PID, E, ID>,
            vararg selections: Selection<*>
        ): ConfigurableTypedSubQuery<P, PID, E, ID, R> =
            ConfigurableTypedSubQueryImpl(
                TypedQueryData(selections.toList()),
                baseQuery = query
            )
    }
}