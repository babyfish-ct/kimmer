package org.babyfish.kimmer.sql.ast.common

import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.PaginationContext
import org.babyfish.kimmer.sql.runtime.dialect.DefaultDialect
import org.babyfish.kimmer.sql.runtime.dialect.UpdateJoin

class DynamicDialect: Dialect {

    private val defaultDialect = DefaultDialect()

    private var overrideDialect: Dialect? = null

    override val updateJoin: UpdateJoin?
        get() = (overrideDialect ?: defaultDialect).updateJoin

    override fun pagination(ctx: PaginationContext) {
        (overrideDialect ?: defaultDialect).pagination(ctx)
    }

    fun using(dialect: Dialect, block: () -> Unit) {
        overrideDialect = dialect
        try {
            block()
        } finally {
            overrideDialect = null
        }
    }
}