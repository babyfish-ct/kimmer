package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.PaginationContext

class SqlServerDialect : DefaultDialect() {

    override val updateJoin: UpdateJoin?
        get() = UpdateJoin(true, UpdateJoin.From.AS_ROOT)

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
}