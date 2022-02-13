package org.babyfish.kimmer.sql.meta.config

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.table.Table

interface Formula<E: Entity<ID>, ID: Comparable<ID>, T>: Storage {

    fun get(table: Table<E, ID>): Expression<T>

    companion object {

        @JvmStatic
        fun <E: Entity<ID>, ID: Comparable<ID>, T> of(
            block: Table<E, ID>.() -> Expression<T>
        ): Formula<E, ID, T> =
            object : Formula<E, ID, T> {
                override fun get(table: Table<E, ID>): Expression<T> =
                    table.block()
            }
    }
}