package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import kotlin.reflect.KProperty1

sealed interface Table<E: Entity<ID>, ID: Comparable<ID>> {

    val id: Expression<ID>

    fun <X: Any> get(
        prop: KProperty1<E, X>
    ): Expression<X>

    fun <X: Any> `get?`(
        prop: KProperty1<E, X?>
    ): Expression<X>
}