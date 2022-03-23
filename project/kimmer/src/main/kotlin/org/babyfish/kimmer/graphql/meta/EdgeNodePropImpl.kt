package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.impl.NODE
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class EdgeNodePropImpl(
    override val declaringType: EdgeTypeImpl
) : ImmutableProp {

    override val name: String
        get() = NODE

    override val kotlinProp: KProperty1<*, *>
        get() = Connection.Edge<*>::node

    override val returnType: KClass<*>
        get() = declaringType.nodeType.kotlinType

    override val javaReturnType: Class<*>
        get() = declaringType.nodeType.kotlinType.java

    override val targetType: ImmutableType?
        get() = declaringType.nodeType

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

    override val isTargetNullable: Boolean
        get() = false

    override fun toString(): String =
        kotlinProp.toString()
}