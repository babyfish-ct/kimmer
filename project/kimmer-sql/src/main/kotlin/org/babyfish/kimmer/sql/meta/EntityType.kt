package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import kotlin.reflect.KClass

interface EntityType {

    val immutableType: ImmutableType

    @Suppress("UNCHECKED_CAST")
    val kotlinType: KClass<out Entity<*>>
        get() = immutableType.kotlinType as KClass<out Entity<*>>

    val superType: EntityType?

    val derivedTypes: List<EntityType>

    val tableName: String

    val idProp: EntityProp

    val idGenerator: IdGenerator?

    val versionProp: EntityProp?

    val declaredProps: Map<String, EntityProp>

    val props: Map<String, EntityProp>

    val starProps: Map<String, EntityProp>

    val backProps: Set<EntityProp>
}