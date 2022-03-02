package org.babyfish.kimmer.sql.runtime.dialect

class PostgresDialect : DefaultDialect() {

    override val updateJoin: UpdateJoin?
        get() = UpdateJoin(false, UpdateJoin.From.AS_JOIN)
}