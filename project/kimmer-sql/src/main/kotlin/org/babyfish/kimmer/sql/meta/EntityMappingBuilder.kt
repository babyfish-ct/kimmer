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

    fun entity(
        type: KClass<out Entity<*>>,
        tableName: String? = null,
        idGenerator: IdGenerator? = null,
        versionProp: KProperty1<*, Int>? = null
    ): EntityTypeImpl

    fun prop(
        prop: KProperty1<out Entity<*>, *>,
        storage: Storage? = null
    ): EntityPropImpl

    fun inverseProp(
        prop: KProperty1<out Entity<*>, *>,
        mappedBy: KProperty1<out Entity<*>, *>
    ): EntityPropImpl

    fun transientProp(
        prop: KProperty1<out Entity<*>, *>
    )

    fun storage(
        prop: KProperty1<out Entity<*>, *>,
        storage: Storage
    )

    fun scalarProvider(
        scalarProvider: ScalarProvider<*, *>
    )
}