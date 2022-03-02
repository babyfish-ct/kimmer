package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.PaginationContext

class MysqlDialect: DefaultDialect() {

    override val updateJoin: UpdateJoin?
        get() = UpdateJoin(true, UpdateJoin.From.UNNECESSARY)

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