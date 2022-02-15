package org.babyfish.kimmer.sql.spi

import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.runtime.dialect.DefaultDialect

fun createSqlClient(
    dialect: Dialect? = null,
    jdbcExecutor: JdbcExecutor = defaultJdbcExecutor,
    r2dbcExecutor: R2dbcExecutor = defaultR2dbcExecutor,
    block: EntityMappingBuilder.() -> Unit
): SqlClient {

    val entityMap = EntityMappingBuilderImpl().run {
        block()
        build()
    }
    return SqlClientImpl(
        entityMap,
        dialect ?: DefaultDialect(),
        jdbcExecutor,
        r2dbcExecutor
    )
}