package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable

internal class TypedQueryImpl<T: Immutable, R>(
    private val selections: List<Selection<*>>,
    private val baseQuery: QueryImpl<T>
) : TypedSqlQuery<T, R>, SqlQuery<T> by (baseQuery), Renderable {

    @Suppress("UNCHECKED_CAST")
    override fun execute(con: java.sql.Connection): List<R> {
        val (sql, variables) = JdbcSqlBuilder().apply {
            renderTo(this)
        }.build()
        val executor = baseQuery.sql.jdbcExecutor
        if (executor !== null) {
            return executor(sql, variables) as List<R>
        }
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(con: io.r2dbc.spi.Connection): List<R> {
        val (sql, variables) = R2dbcSqlBuilder().apply {
            renderTo(this)
        }.build()
        val executor = baseQuery.sql.r2dbcExecutor
        if (executor !== null) {
            return executor(sql, variables) as List<R>
        }
        TODO("Not yet implemented")
    }

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("select ")
        var sp: String? = null
        for (selection in selections) {
            if (sp !== null) {
                builder.sql(sp)
            } else {
                sp = ", "
            }
            (selection as Renderable).renderTo(builder)
        }
        baseQuery.renderWithoutSelection(builder)
    }
}