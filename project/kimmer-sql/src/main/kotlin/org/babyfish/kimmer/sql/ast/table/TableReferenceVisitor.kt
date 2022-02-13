package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.sql.Selection
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.SqlBuilder
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

internal fun Selection<*>.accept(visitor: TableReferenceVisitor) {
    when (this) {
        is JoinableTable<*, *> -> {
            if ((this as TableImpl<*, *>).entityType.starProps.size > 1) {
                visitor.visit(this, null)
            } else Unit
        }
        is Expression<*> -> (this as TableReferenceElement).accept(visitor)
        else -> error("Internal bug")
    }
}