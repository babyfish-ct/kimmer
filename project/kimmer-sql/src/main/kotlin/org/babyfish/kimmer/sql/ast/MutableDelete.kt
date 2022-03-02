package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.table.NonNullTable

interface MutableDelete<E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullTable<E, ID>

    fun where(vararg predicates: NonNullExpression<Boolean>?)

    fun where(block: () -> NonNullExpression<Boolean>?)
}