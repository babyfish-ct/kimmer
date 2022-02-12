package org.babyfish.kimmer.sql.spi

import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor

fun createSqlClient(
    jdbcExecutor: ((String, List<Any?>) -> List<*>)? = null,
    r2dbcExecutor: R2dbcExecutor,
    block: EntityMappingBuilder.() -> Unit
): SqlClient {

    val entityMap = EntityMappingBuilderImpl().run {
        block()
        build()
    }

    return SqlClientImpl(entityMap, jdbcExecutor, r2dbcExecutor)
}