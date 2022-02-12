package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass

interface EntityType {

    val name: String

    val immutableType: ImmutableType

    val kotlinType: KClass<out Immutable>
        get() = immutableType.kotlinType

    val superType: EntityType?

    val derivedTypes: List<EntityType>

    val tableName: String

    val idProp: EntityProp

    val isMapped: Boolean

    val declaredProps: Map<String, EntityProp>

    val props: Map<String, EntityProp>

    val starProps: Map<String, EntityProp>
}