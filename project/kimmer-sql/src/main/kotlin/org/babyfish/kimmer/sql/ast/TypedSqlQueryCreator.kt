package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity

interface TypedSqlQueryCreator<E: Entity<ID>, ID: Comparable<ID>, R> {

    fun create(
        block: (Context.() -> Unit)? = null
    ): TypedSqlQuery<E, ID, R>

    interface Context {

        fun limit(limit: Int, offset: Int = 0)

        fun withoutSortingAndPaging(without: Boolean = true)
    }
}
