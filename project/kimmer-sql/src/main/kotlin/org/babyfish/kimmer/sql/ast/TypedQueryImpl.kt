package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityProp

internal class TypedQueryImpl<E, ID, R>(
    private val selections: List<Selection<*>>,
    private val baseQuery: QueryImpl<E, ID>,
    private val limit: Int = Int.MAX_VALUE,
    private val offset: Int = 0,
    private val withoutSortingAndPaging: Boolean = false
) : TypedSqlQuery<E, ID, R>,
    SqlQuery<E, ID> by (baseQuery),
    Renderable,
    TableReferenceElement
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    override fun create(
        block: TypedSqlQueryCreator.Context.() -> Unit
    ): TypedSqlQuery<E, ID, R> =
        NewCreator().create(block)

    override fun <X> creator(
        block: SqlQuery<E, ID>.() -> TypedSqlQuery<E, ID, X>
    ): TypedSqlQueryCreator<E, ID, X> =
        (block() as TypedQueryImpl<E, ID, X>).NewCreator()

    @Suppress("UNCHECKED_CAST")
    override fun execute(con: java.sql.Connection): List<R> {
        val (sql, variables) = prepare(JdbcSqlBuilder())
        val executor = baseQuery.sql.jdbcExecutor
        if (executor !== null) {
            return executor(sql, variables) as List<R>
        }
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(con: io.r2dbc.spi.Connection): List<R> {
        val (sql, variables) = prepare(R2dbcSqlBuilder())
        val executor = baseQuery.sql.r2dbcExecutor
        if (executor !== null) {
            return executor(sql, variables) as List<R>
        }
        TODO("Not yet implemented")
    }

    private fun prepare(builder: SqlBuilder): Pair<String, List<Any?>> {
        val visitor = UseTableVisitor(builder)
        accept(visitor)
        renderTo(builder)
        return builder.build()
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
            if (selection is TableImpl<*, *>) {
                selection.renderAsSelection(builder)
            } else {
                (selection as Renderable).renderTo(builder)
            }
        }
        baseQuery.renderTo(builder, withoutSortingAndPaging)
        if (!withoutSortingAndPaging) {
            builder.apply {
                if (limit != Int.MAX_VALUE) {
                    sql(" limit ")
                    variable(limit)
                }
                if (offset > 0) {
                    sql(" offset ")
                    variable(offset)
                }
            }
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selections.forEach { it.accept(visitor) }
        baseQuery.accept(visitor, withoutSortingAndPaging)
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

    private inner class NewCreator: TypedSqlQueryCreator<E, ID, R> {

        private var limit = this@TypedQueryImpl.limit

        private var offset = this@TypedQueryImpl.offset

        private var withoutSortingAndPaging = this@TypedQueryImpl.withoutSortingAndPaging

        override fun create(
            block: (TypedSqlQueryCreator.Context.() -> Unit)?
        ): TypedSqlQuery<E, ID, R> {
            if (block !== null) {
                ContextImpl().block()
            }
            return if (this@TypedQueryImpl.limit == limit &&
                this@TypedQueryImpl.offset == offset &&
                this@TypedQueryImpl.withoutSortingAndPaging == withoutSortingAndPaging
            ) {
                this@TypedQueryImpl
            } else {
                TypedQueryImpl(selections, baseQuery, limit, offset, withoutSortingAndPaging)
            }
        }

        private inner class ContextImpl: TypedSqlQueryCreator.Context {

            override fun limit(limit: Int, offset: Int) {
                if (limit < 1) {
                    throw IllegalArgumentException("'limit' must be greater than 0")
                }
                if (offset < 0) {
                    throw IllegalArgumentException("'offset' cannot be negative")
                }
                this@NewCreator.limit = limit
                this@NewCreator.offset = offset
            }

            override fun withoutSortingAndPaging(without: Boolean) {
                withoutSortingAndPaging = without
            }
        }
    }
}