package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.config.Storage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface EntityProp {

    val declaringType: EntityType

    val immutableProp: ImmutableProp

    val kotlinProp: KProperty1<*, *>
        get() = immutableProp.kotlinProp

    val name: String
        get() = kotlinProp.name

    val isId: Boolean

    val isVersion: Boolean

    val returnType: KClass<*>

    /** Be different with [returnType].java */
    val javaReturnType: Class<*>

    val isReference: Boolean

    val isList: Boolean

    val isConnection: Boolean

    val isNullable: Boolean

    val isTargetNullable: Boolean

    val targetType: EntityType?

    val mappedBy: EntityProp? // Same as opposite if not null

    val opposite: EntityProp?

    val scalarProvider: ScalarProvider<*, *>?

    val storage: Storage?
}