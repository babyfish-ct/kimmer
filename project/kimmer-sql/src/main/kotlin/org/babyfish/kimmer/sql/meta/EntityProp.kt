package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
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

    val oppositeProp: EntityProp?

    val isId: Boolean

    val returnType: KClass<*>

    val isReference: Boolean

    val isList: Boolean

    val isConnection: Boolean

    val isNullable: Boolean

    val isTargetNullable: Boolean

    val targetType: EntityType?

    val mappedBy: EntityProp? // Same as opposite if not null

    val opposite: EntityProp?

    val storage: Storage?
}