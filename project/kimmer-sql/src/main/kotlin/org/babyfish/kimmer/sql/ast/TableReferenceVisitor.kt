package org.babyfish.kimmer.sql.ast

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
    }.apply {
        // This empty "apply" make compiler guarantee all the derived types
        // of the sealed super interface have been checked by the 'when' statement
    }
}