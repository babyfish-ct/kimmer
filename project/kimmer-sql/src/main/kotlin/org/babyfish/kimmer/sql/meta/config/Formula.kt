package org.babyfish.kimmer.sql.meta.config

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.table.NonNullTable

interface Formula<E: Entity<ID>, ID: Comparable<ID>, T: Any>: Storage {

    fun get(table: NonNullTable<E, ID>): Expression<T>

    companion object {

        @JvmStatic
        fun <E: Entity<ID>, ID: Comparable<ID>, T: Any> of(
            block: NonNullTable<E, ID>.() -> Expression<T>
        ): Formula<E, ID, T> =
            object : Formula<E, ID, T> {
                override fun get(table: NonNullTable<E, ID>): Expression<T> =
                    table.block()
            }
    }
}