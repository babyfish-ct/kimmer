package org.babyfish.kimmer.sql.common

import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.PaginationContext
import org.babyfish.kimmer.sql.runtime.dialect.DefaultDialect
import org.babyfish.kimmer.sql.runtime.dialect.UpdateJoin

class DynamicDialect: Dialect {

    private val defaultDialect = DefaultDialect()

    private var overrideDialect: Dialect? = null

    fun using(dialect: Dialect, block: () -> Unit) {
        val old = overrideDialect
        overrideDialect = dialect
        try {
            block()
        } finally {
            overrideDialect = old
        }
    }

    override fun r2dbcParameter(position: Int): String =
        (overrideDialect ?: defaultDialect).r2dbcParameter(position)

    override fun pagination(ctx: PaginationContext) {
        (overrideDialect ?: defaultDialect).pagination(ctx)
    }

    override val updateJoin: UpdateJoin?
        get() = (overrideDialect ?: defaultDialect).updateJoin

    override fun idFromSequenceSql(sequenceName: String): String =
        (overrideDialect ?: defaultDialect).idFromSequenceSql(sequenceName)

    override val lastIdentitySql: String
        get() = (overrideDialect ?: defaultDialect).lastIdentitySql

    override val overrideIdentityIdSql: String?
        get() = (overrideDialect ?: defaultDialect).overrideIdentityIdSql
}