package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.JdbcSqlBuilder
import org.babyfish.kimmer.sql.ast.R2dbcSqlBuilder
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.selectable.RootSelectable
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.runtime.JdbcExecutorContext
import org.babyfish.kimmer.sql.runtime.R2dbcExecutorContext

internal class ConfigurableTypedRootQueryImpl<E, ID, R>(
    data: TypedQueryData,
    baseQuery: RootQueryImpl<E, ID>
): AbstractConfigurableTypedQueryImpl<E, ID, R>(
    data,
    baseQuery
), ConfigurableTypedRootQuery<E, ID, R>
    where E: Entity<ID>, ID: Comparable<ID> {

    override val baseQuery: RootQueryImpl<E, ID>
        get() = super.baseQuery as RootQueryImpl<E, ID>
    
    override fun <X: Any> reselect(
        block: RootSelectable<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, X>
    ): ConfigurableTypedRootQuery<E, ID, X> {
        val reselected = baseQuery.block()
        val selections = (reselected as ConfigurableTypedRootQueryImpl<E, ID, X>).data.selections
        return ConfigurableTypedRootQueryImpl(
            data = data.copy(selections = selections),
            baseQuery = baseQuery
        )
    }

    override fun distinct(distinct: Boolean): ConfigurableTypedRootQuery<E, ID, R> =
        if (data.distinct == distinct) {
            this
        } else {
            ConfigurableTypedRootQueryImpl(
                data.copy(distinct = distinct),
                baseQuery
            )
        }

    override fun limit(limit: Int, offset: Int): ConfigurableTypedRootQuery<E, ID, R> =
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
            ConfigurableTypedRootQueryImpl(
                data = data.copy(limit = limit, offset = offset),
                baseQuery = baseQuery
            )
        }

    override fun withoutSortingAndPaging(without: Boolean): ConfigurableTypedRootQuery<E, ID, R> =
        if (data.withoutSortingAndPaging == without) {
            this
        } else {
            ConfigurableTypedRootQueryImpl(
                data = data.copy(withoutSortingAndPaging = without),
                baseQuery = baseQuery
            )
        }

    @Suppress("UNCHECKED_CAST")
    override fun execute(con: java.sql.Connection): List<R> {
        val (sql, variables) = preExecute(JdbcSqlBuilder())
        val executor = baseQuery.sqlClient.jdbcExecutor
        return executor(JdbcExecutorContext(con, data.selections, sql, variables)) as List<R>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(con: io.r2dbc.spi.Connection): List<R> {
        val (sql, variables) = preExecute(R2dbcSqlBuilder())
        val executor = baseQuery.sqlClient.r2dbcExecutor
        return executor(R2dbcExecutorContext(con, data.selections, sql, variables)) as List<R>
    }

    private fun preExecute(builder: SqlBuilder): Pair<String, List<Any>> {
        val visitor = UseTableVisitor(builder)
        accept(visitor)
        renderTo(builder)
        return builder.build()
    }

    override fun union(right: TypedRootQuery<E, ID, R>): TypedRootQuery<E, ID, R> =
        MergedTypedRootQueryImpl(baseQuery.sqlClient, "union", this, right)

    override fun unionAll(right: TypedRootQuery<E, ID, R>): TypedRootQuery<E, ID, R> =
        MergedTypedRootQueryImpl(baseQuery.sqlClient, "union all", this, right)

    override fun minus(right: TypedRootQuery<E, ID, R>): TypedRootQuery<E, ID, R> =
        MergedTypedRootQueryImpl(baseQuery.sqlClient, "minus", this, right)

    override fun intersect(right: TypedRootQuery<E, ID, R>): TypedRootQuery<E, ID, R> =
        MergedTypedRootQueryImpl(baseQuery.sqlClient, "intersect", this, right)

    companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <E: Entity<ID>, ID: Comparable<ID>, R> select(
            query: RootQueryImpl<E, ID>,
            selections: List<Selection<*>>
        ): ConfigurableTypedRootQuery<E, ID, R> =
            ConfigurableTypedRootQueryImpl(
                TypedQueryData(selections.toList()),
                baseQuery = query
            )
    }
}
