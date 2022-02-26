package org.babyfish.kimmer.sql.spi

import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.meta.spi.DefaultMetaFactory
import org.babyfish.kimmer.sql.meta.spi.MetaFactory
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.runtime.dialect.DefaultDialect

fun createSqlClient(
    dialect: Dialect? = null,
    jdbcExecutor: JdbcExecutor = defaultJdbcExecutor,
    r2dbcExecutor: R2dbcExecutor = defaultR2dbcExecutor,
    metaFactory: MetaFactory? = null,
    block: EntityMappingBuilder.() -> Unit
): SqlClient {

    val (entityMap, entityProviderMap) = EntityMappingBuilderImpl(
        metaFactory ?: DefaultMetaFactory
    ).run {
        block()
        build()
    }
    return SqlClientImpl(
        entityMap,
        entityProviderMap,
        dialect ?: DefaultDialect(),
        jdbcExecutor,
        r2dbcExecutor
    )
}