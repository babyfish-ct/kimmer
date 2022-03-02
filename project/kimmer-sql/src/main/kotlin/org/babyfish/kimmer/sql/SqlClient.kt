package org.babyfish.kimmer.sql

import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.ast.MutableDelete
import org.babyfish.kimmer.sql.ast.MutableUpdate
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.runtime.Dialect
import kotlin.reflect.KClass

interface SqlClient {

    val entityTypeMap: Map<KClass<out Entity<*>>, EntityType>

    val scalarProviderMap: Map<KClass<*>, ScalarProvider<*, *>>

    val dialect: Dialect

    fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R>

    fun <E: Entity<ID>, ID: Comparable<ID>> createUpdate(
        type: KClass<E>,
        block: MutableUpdate<E, ID>.() -> Unit
    ): Executable<Int>

    fun <E: Entity<ID>, ID: Comparable<ID>> createDelete(
        type: KClass<E>,
        block: MutableDelete<E, ID>.() -> Unit
    ): Executable<Int>

    val entities: Entities

    interface Entities {

        fun <E: Entity<*>> save(
            entity: E,
            options: SaveOptions<E>? = null
        )

        fun <E: Entity<*>> save(
            entity: List<E>,
            options: SaveOptions<E>? = null
        )

        fun <E: Entity<ID>, ID: Comparable<ID>> delete(
            type: KClass<E>,
            id: ID
        ): Int

        fun <E: Entity<ID>, ID: Comparable<ID>> delete(
            type: KClass<E>,
            ids: Collection<ID>
        ): Int
    }
}
