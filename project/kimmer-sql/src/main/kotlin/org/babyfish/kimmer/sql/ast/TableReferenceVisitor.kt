package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.meta.EntityProp

internal interface TableReferenceVisitor {
    fun visit(table: Table<*, *>, entityProp: EntityProp?)
}

internal interface TableReferenceElement {
    fun accept(visitor: TableReferenceVisitor)
}

internal fun Selection<*>.accept(visitor: TableReferenceVisitor) {
    when (this) {
        is JoinableTable<*, *> -> visitor.visit(this, null)
        is Expression<*> -> (this as TableReferenceElement).accept(visitor)
    }.apply {
        // This empty "apply" make compiler guarantee all the derived types
        // of the sealed super interface have been checked by the 'when' statement
    }
}