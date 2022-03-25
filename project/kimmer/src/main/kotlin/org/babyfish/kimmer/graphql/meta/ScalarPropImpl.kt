package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class ScalarPropImpl(
    override val declaringType: ImmutableType,
    override val kotlinProp: KProperty1<*, *>
) : ImmutableProp {

    override val name: String
        get() = kotlinProp.name

    override val returnType: KClass<*>
        get() = kotlinProp.returnType.classifier as KClass<*>

    override val javaReturnType: Class<*>
        get() = returnType.java

    override val isNullable: Boolean
        get() = false

    override val isAssociation: Boolean
        get() = false

    override val isReference: Boolean
        get() = false

    override val isList: Boolean
        get() = false

    override val isConnection: Boolean
        get() = false

    override val isScalarList: Boolean
        get() = false

    override val elementType: KClass<*>
        get() = returnType

    override val targetType: ImmutableType?
        get() = null

    override val isElementNullable: Boolean
        get() = false

    override fun toString(): String =
        kotlinProp.toString()
}