package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.PaginationContext

class SqlServerDialect : DefaultDialect() {

    override fun r2dbcParameter(position: Int): String =
        "@$position"

    override fun pagination(ctx: PaginationContext) {
        ctx.apply {
            origin()
            sql(" offset ")
            variable(offset)
            sql(" rows fetch next ")
            variable(limit)
            sql(" rows only")
        }
    }

    override val updateJoin: UpdateJoin?
        get() = UpdateJoin(true, UpdateJoin.From.AS_ROOT)

    override val lastIdentitySql: String
        get() = "select @@identity"
}