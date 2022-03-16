package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.PaginationContext

class MysqlDialect: DefaultDialect() {

    override fun r2dbcParameter(position: Int): String =
        "?"

    override fun pagination(ctx: PaginationContext) {
        ctx.apply {
            origin()
            sql(" limit ")
            variable(offset)
            sql(", ")
            variable(limit)
        }
    }

    override val updateJoin: UpdateJoin?
        get() = UpdateJoin(true, UpdateJoin.From.UNNECESSARY)

    override val lastIdentitySql: String
        get() = "select last_insert_id()"
}