package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.sql.runtime.dialect.UpdateJoin

interface Dialect {

    val updateJoin: UpdateJoin?

    fun pagination(ctx: PaginationContext)
}