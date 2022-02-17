package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.spi.Renderable

internal class Order(
    private val expression: Expression<*>,
    private val mode: OrderMode,
    private val nullMode: NullOrderMode
): Renderable, Ast {

    override fun renderTo(builder: SqlBuilder) {
        (expression as Renderable).renderTo(builder)
        builder.apply {
            sql(" ")
            sql(if (mode == OrderMode.DESC) "desc" else "asc")
            if (nullMode != NullOrderMode.UNSPECIFIED) {
                sql(" ")
                sql(if (nullMode === NullOrderMode.NULLS_FIRST) "nulls first" else "nulls last")
            }
        }
    }

    override fun accept(visitor: AstVisitor) {
        expression.accept(visitor)
    }
}