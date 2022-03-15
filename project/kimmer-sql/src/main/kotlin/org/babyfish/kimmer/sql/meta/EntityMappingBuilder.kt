package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.meta.config.UserIdGenerator
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import org.babyfish.kimmer.sql.meta.spi.EntityTypeImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * This is SPI, not API！
 *
 * Its design goal is to ensure the least number of interfaces,
 * not to provide a sufficiently secure interface.
 *
 * Higher-level frameworks will encapsulate this to provide
 * more complex but also safer APIs
 */
interface EntityMappingBuilder {

    fun entity(type: KClass<out Entity<*>>): EntityTypeImpl

    fun tableName(type: KClass<out Entity<*>>, tableName: String)

    fun prop(
        prop: KProperty1<out Entity<*>, *>,
        storage: Storage? = null,
        idGenerator: IdGenerator? = null,
        isVersion: Boolean = false
    ): EntityPropImpl

    fun <ID: Comparable<ID>> prop(
        prop: KProperty1<out Entity<ID>, ID>,
        storage: Storage? = null,
        idGenerator: UserIdGenerator<ID>,
        isVersion: Boolean = false
    ): EntityPropImpl

    fun inverseProp(prop: KProperty1<out Entity<*>, *>, mappedBy: KProperty1<out Entity<*>, *>): EntityPropImpl

    fun storage(prop: KProperty1<out Entity<*>, *>, storage: Storage)

    fun scalar(scalarProvider: ScalarProvider<*, *>)
}