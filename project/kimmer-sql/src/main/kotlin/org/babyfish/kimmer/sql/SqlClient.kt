package org.babyfish.kimmer.sql

import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.ast.MutableDelete
import org.babyfish.kimmer.sql.ast.MutableUpdate
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.Queries
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import kotlin.reflect.KClass

interface SqlClient {

    val entityTypeMap: Map<KClass<out Entity<*>>, EntityType>

    val scalarProviderMap: Map<KClass<*>, ScalarProvider<*, *>>

    val dialect: Dialect

    val jdbcExecutor: JdbcExecutor

    val r2dbcExecutor: R2dbcExecutor

    fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R> =
        queries.byType(type, block)

    fun <E : Entity<ID>, ID : Comparable<ID>> createUpdate(
        type: KClass<E>,
        block: MutableUpdate<E, ID>.() -> Unit
    ): Executable<Int>

    fun <E : Entity<ID>, ID : Comparable<ID>> createDelete(
        type: KClass<E>,
        block: MutableDelete<E, ID>.() -> Unit
    ): Executable<Int>

    val queries: Queries

    val entities: Entities

    //val trigger: Trigger
}
