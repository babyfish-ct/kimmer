package org.babyfish.kimmer.sql.runtime

interface Dialect {

    fun pagination(ctx: PaginationContext)
}