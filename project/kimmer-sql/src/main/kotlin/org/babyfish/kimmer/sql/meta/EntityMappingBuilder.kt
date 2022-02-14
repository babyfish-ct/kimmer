package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.meta.config.Storage
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

    fun tableName(type: KClass<out Immutable>, tableName: String)

    fun prop(prop: KProperty1<out Immutable, *>, storage: Storage? = null)

    fun inverseProp(prop: KProperty1<out Immutable, *>, mappedBy: KProperty1<out Immutable, *>)

    fun storage(prop: KProperty1<out Immutable, *>, storage: Storage)

    fun build(): Map<KClass<out Immutable>, EntityType>
}