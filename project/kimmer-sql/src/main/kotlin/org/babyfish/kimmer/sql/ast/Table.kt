package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KProperty1

sealed interface Table<E: Entity<ID>, ID: Comparable<ID>> {

    val id: Expression<ID>

    operator fun <X> get(
        prop: KProperty1<E, X?>
    ): Expression<X>
}