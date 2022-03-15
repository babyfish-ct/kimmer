package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.PaginationContext

open class DefaultDialect : Dialect {

    override fun pagination(ctx: PaginationContext) {
        ctx.apply {
            origin()
            sql(" limit ")
            variable(limit)
            sql(" offset ")
            variable(offset)
        }
    }

    override val updateJoin: UpdateJoin?
        get() = null

    override fun idFromSequenceSql(sequenceName: String): String =
        throw ExecutionException("Sequence is not supported by '${this::class.qualifiedName}'")

    override val lastIdentitySql: String
        get() = throw ExecutionException("Identity is not supported by '${this::class.qualifiedName}'")

    override val overrideIdentityIdSql: String?
        get() = null
}