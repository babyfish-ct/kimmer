package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.Storage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface EntityMappingBuilder {

    fun tableName(type: KClass<out Immutable>, tableName: String)

    fun id(prop: KProperty1<out Immutable, *>, storage: Storage? = null)

    fun prop(prop: KProperty1<out Immutable, *>, storage: Storage? = null)

    fun inverseAssociation(prop: KProperty1<out Immutable, *>, mappedBy: KProperty1<out Immutable, *>)

    fun storage(prop: KProperty1<out Immutable, *>, storage: Storage)

    fun build(): Map<KClass<out Immutable>, EntityType>
}