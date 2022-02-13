package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.accept

internal class Order(
    private val expression: Expression<*>,
    private val descending: Boolean
): Renderable, TableReferenceElement {

    override fun renderTo(builder: SqlBuilder) {
        (expression as Renderable).renderTo(builder)
        builder.apply {
            sql(" ")
            sql(if (descending) "desc" else "asc")
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
    }
}