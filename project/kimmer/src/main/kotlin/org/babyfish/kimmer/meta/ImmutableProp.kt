package org.babyfish.kimmer.meta

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface ImmutableProp {

    val declaringType: ImmutableType

    val kotlinProp: KProperty1<*, *>

    val returnType: KClass<*>

    /** Be different with [returnType].java */
    val javaReturnType: Class<*>

    val isNullable: Boolean

    val isScalarList: Boolean

    val elementType: KClass<*>

    val isElementNullable: Boolean

    val isAssociation: Boolean

    val isReference: Boolean

    val isList: Boolean

    val isConnection: Boolean

    val targetType: ImmutableType?

    val name: String
        get() = kotlinProp.name
}