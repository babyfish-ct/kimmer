package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.JdbcSqlBuilder
import org.babyfish.kimmer.sql.ast.R2dbcSqlBuilder
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.babyfish.kimmer.sql.ast.AstVisitor
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.runtime.JdbcSelector
import org.babyfish.kimmer.sql.runtime.R2dbcSelector

internal class MergedTypedRootQueryImpl<R>(
    private val sqlClient: SqlClientImpl,
    private val operator: String,
    left: TypedRootQuery<R>,
    right: TypedRootQuery<R>
) :
    TypedRootQuery<R>,
    TypedQueryImplementor {

    private val _selections: List<Selection<*>>

    private val left: TypedQueryImplementor

    private val right: TypedQueryImplementor

    init {
        this.left = left as TypedQueryImplementor
        this.right = right as TypedQueryImplementor
        this._selections = mergedSelections(left, right)
    }

    override val selections: List<Selection<*>>
        get() = _selections

    @Suppress("UNCHECKED_CAST")
    override fun execute(con: java.sql.Connection): List<R> {
        val (sql, variables) = preExecute(JdbcSqlBuilder(sqlClient))
        return JdbcSelector(sqlClient, selections).select(con, sql, variables) as List<R>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(con: io.r2dbc.spi.Connection): List<R> {
        val (sql, variables) = preExecute(R2dbcSqlBuilder(sqlClient))
        return R2dbcSelector(sqlClient, selections).select(con, sql, variables) as List<R>
    }

    override fun renderTo(builder: SqlBuilder) {
        left.renderTo(builder)
        builder.sql(" ")
        builder.sql(operator)
        builder.sql(" ")
        right.renderTo(builder)
    }

    override fun accept(visitor: AstVisitor) {
        left.accept(visitor)
        right.accept(visitor)
    }

    private fun preExecute(builder: AbstractSqlBuilder): Pair<String, List<Any>> {
        val visitor = UseTableVisitor(builder)
        accept(visitor)
        renderTo(builder)
        return builder.build()
    }

    override fun union(right: TypedRootQuery<R>): TypedRootQuery<R> =
        MergedTypedRootQueryImpl(sqlClient, "union", this, right)

    override fun unionAll(right: TypedRootQuery<R>): TypedRootQuery<R> =
        MergedTypedRootQueryImpl(sqlClient, "union all", this, right)

    override fun minus(right: TypedRootQuery<R>): TypedRootQuery<R> =
        MergedTypedRootQueryImpl(sqlClient, "minus", this, right)

    override fun intersect(right: TypedRootQuery<R>): TypedRootQuery<R> =
        MergedTypedRootQueryImpl(sqlClient, "intersect", this, right)
}