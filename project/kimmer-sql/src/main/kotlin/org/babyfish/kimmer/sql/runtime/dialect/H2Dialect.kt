package org.babyfish.kimmer.sql.runtime.dialect

class H2Dialect : DefaultDialect() {

    override fun idFromSequenceSql(sequenceName: String): String =
        "select nextval('$sequenceName')"

    override val lastIdentitySql: String
        get() = "call scopeidentity()"
}