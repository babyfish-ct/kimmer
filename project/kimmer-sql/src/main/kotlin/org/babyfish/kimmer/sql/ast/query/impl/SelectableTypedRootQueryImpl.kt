package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.R2dbcSqlBuilder
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.selectable.RootSelectable
import org.babyfish.kimmer.sql.ast.query.SelectableTypedRootQuery
import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.runtime.R2dbcExecutorContext

internal class SelectableTypedRootQueryImpl<E, ID, R>(
    data: TypedQueryData,
    baseQuery: RootQueryImpl<E, ID>
): AbstractSelectableTypedQueryImpl<E, ID, R>(
    data,
    baseQuery
), SelectableTypedRootQuery<E, ID, R>
    where E: Entity<ID>, ID: Comparable<ID> {

    override val baseQuery: RootQueryImpl<E, ID>
        get() = super.baseQuery as RootQueryImpl<E, ID>
    
    override fun <X>  reselect(
        block: RootSelectable<E, ID>.() -> SelectableTypedRootQuery<E, ID, X>
    ): SelectableTypedRootQuery<E, ID, X> {
        val reselected = baseQuery.block()
        val selections = (reselected as SelectableTypedRootQueryImpl<E, ID, X>).data.selections
        return SelectableTypedRootQueryImpl(
            data = data.copy(selections = selections),
            baseQuery = baseQuery
        )
    }

    override fun limit(limit: Int, offset: Int): SelectableTypedRootQuery<E, ID, R> =
        if (data.limit == limit && data.offset == offset) {
            this
        } else {
            if (limit < 0) {
                throw IllegalArgumentException("'limit' can not be less than 0")
            }
            if (offset < 0) {
                throw IllegalArgumentException("'offset' can not be less than 0")
            }
            SelectableTypedRootQueryImpl(
                data = data.copy(limit = limit, offset = offset),
                baseQuery = baseQuery
            )
        }

    override fun withoutSortingAndPaging(without: Boolean): SelectableTypedRootQuery<E, ID, R> =
        if (data.withoutSortingAndPaging == without) {
            this
        } else {
            SelectableTypedRootQueryImpl(
                data = data.copy(withoutSortingAndPaging = without),
                baseQuery = baseQuery
            )
        }

    override fun execute(con: java.sql.Connection): List<R> {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(con: io.r2dbc.spi.Connection): List<R> {
        val (sql, variables) = preExecute(R2dbcSqlBuilder())
        val executor = baseQuery.sqlClient.r2dbcExecutor
        return executor(R2dbcExecutorContext(con, sql, variables)) as List<R>
    }

    private fun preExecute(builder: SqlBuilder): Pair<String, List<Any?>> {
        val visitor = UseTableVisitor(builder)
        accept(visitor)
        renderTo(builder)
        return builder.build()
    }

    private class UseTableVisitor(
        override val sqlBuilder: SqlBuilder
    ): TableReferenceVisitor {

        override fun visit(table: TableImpl<*, *>, entityProp: EntityProp?) {
            if (entityProp === null) {
                if (table.entityType.starProps.size > 1) {
                    sqlBuilder.useTable(table)
                }
            } else if (!entityProp.isId) {
                sqlBuilder.useTable(table)
            }
        }
    }

    companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <E: Entity<ID>, ID: Comparable<ID>, R> select(
            query: RootQueryImpl<E, ID>,
            vararg selections: Selection<*>
        ): SelectableTypedRootQuery<E, ID, R> =
            SelectableTypedRootQueryImpl(
                TypedQueryData(selections.toList()),
                baseQuery = query
            )
    }
}
