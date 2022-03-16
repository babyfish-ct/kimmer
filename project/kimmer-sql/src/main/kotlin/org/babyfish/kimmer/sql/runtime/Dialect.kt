package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.sql.runtime.dialect.UpdateJoin

interface Dialect {

    fun r2dbcParameter(position: Int): String

    fun pagination(ctx: PaginationContext)

    val updateJoin: UpdateJoin?

    fun idFromSequenceSql(sequenceName: String): String

    val lastIdentitySql: String

    val overrideIdentityIdSql: String?
}