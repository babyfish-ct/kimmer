package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.PaginationContext

class MysqlDialect: Dialect {

    override fun pagination(ctx: PaginationContext) {
        ctx.apply {
            origin()
            sql(" limit ")
            variable(offset)
            sql(", ")
            variable(limit)
        }
    }
}