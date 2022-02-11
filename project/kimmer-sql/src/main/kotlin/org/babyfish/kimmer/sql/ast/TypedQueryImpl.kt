package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity

internal class TypedQueryImpl<E, ID, R>(
    private val selections: List<Selection<*>>,
    private val baseQuery: QueryImpl<E, ID>
) : TypedSqlQuery<E, ID, R>,
    SqlQuery<E, ID> by (baseQuery),
    Renderable,
    TableReferenceElement
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

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

    override fun accept(visitor: TableReferenceVisitor) {
        selections.forEach { it.accept(visitor) }
    }
}