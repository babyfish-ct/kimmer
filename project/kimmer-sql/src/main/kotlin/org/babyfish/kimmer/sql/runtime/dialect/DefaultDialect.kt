package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.PaginationContext

open class DefaultDialect : Dialect {

    override fun pagination(ctx: PaginationContext) {
        ctx.apply {
            origin()
            sql(" limit ")
            variable(limit)
            sql(" offset ")
            variable(offset)
        }
    }
}