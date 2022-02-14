package org.babyfish.kimmer.sql.spi

import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import org.babyfish.kimmer.sql.runtime.defaultJdbcExecutor
import org.babyfish.kimmer.sql.runtime.defaultR2dbcExecutor

fun createSqlClient(
    jdbcExecutor: JdbcExecutor = defaultJdbcExecutor,
    r2dbcExecutor: R2dbcExecutor = defaultR2dbcExecutor,
    block: EntityMappingBuilder.() -> Unit
): SqlClient {

    val entityMap = EntityMappingBuilderImpl().run {
        block()
        build()
    }

    return SqlClientImpl(entityMap, jdbcExecutor, r2dbcExecutor)
}