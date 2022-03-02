package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.table.NonNullTable

interface MutableUpdate<E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullTable<E, ID>

    fun <X> set(path: NonNullPropExpression<X>, value: NonNullExpression<X>)

    fun <X> set(path: PropExpression<X>, value: Expression<X>)

    fun <X: Any> set(path: NonNullPropExpression<X>, value: X)

    fun <X: Any> set(path: PropExpression<X>, value: X?)

    fun where(vararg predicates: NonNullExpression<Boolean>?)

    fun where(block: () -> NonNullExpression<Boolean>?)
}