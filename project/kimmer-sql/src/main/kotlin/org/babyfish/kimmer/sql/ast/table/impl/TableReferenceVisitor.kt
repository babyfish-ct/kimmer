package org.babyfish.kimmer.sql.ast.table.impl

import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.NonNullExpression
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp

internal interface TableReferenceVisitor {

    val sqlBuilder: SqlBuilder

    fun visit(table: TableImpl<*, *>, entityProp: EntityProp?)

    fun skipSubQuery(): Boolean = false
}

internal interface TableReferenceElement {
    fun accept(visitor: TableReferenceVisitor)
}

internal fun TypedSubQuery<*, *, *, *, *>.accept(visitor: TableReferenceVisitor) {
    (this as TableReferenceElement).accept(visitor)
}

internal fun NonNullExpression<*>.accept(visitor: TableReferenceVisitor) {
    (this as TableReferenceElement).accept(visitor)
}

internal fun Expression<*>.accept(visitor: TableReferenceVisitor) {
    (this as TableReferenceElement).accept(visitor)
}

internal fun Selection<*>.accept(visitor: TableReferenceVisitor) {
    if (this is TableImpl<*, *>) {
        visitor.visit(this, null)
    } else {
        (this as TableReferenceElement).accept(visitor)
    }
}
