package org.babyfish.kimmer.sql.runtime.dialect

class PostgresDialect: DefaultDialect() {

    override val updateJoin: UpdateJoin?
        get() = UpdateJoin(false, UpdateJoin.From.AS_JOIN)

    override fun idFromSequenceSql(sequenceName: String): String =
        "select nextval('$sequenceName')"

    override val lastIdentitySql: String
        get() = "select lastval()"

    override val overrideIdentityIdSql: String
        get() = "overriding system value"
}