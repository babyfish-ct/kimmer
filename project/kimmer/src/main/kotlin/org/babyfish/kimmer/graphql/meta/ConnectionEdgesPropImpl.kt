package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.impl.EDGES
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class ConnectionEdgesPropImpl(
    override val declaringType: ConnectionTypeImpl
) : ImmutableProp {

    override val name: String
        get() = EDGES

    override val kotlinProp: KProperty1<*, *>
        get() = Connection<*>::edges

    override val returnType: KClass<*>
        get() = Connection.Edge::class

    override val javaReturnType: Class<*>
        get() = returnType.java

    override val targetType: ConnectionEdgeType =
        EdgeTypeImpl(declaringType.nodeType)

    override val isNullable: Boolean
        get() = false

    override val isAssociation: Boolean
        get() = true

    override val isReference: Boolean
        get() = false

    override val isList: Boolean
        get() = true

    override val isConnection: Boolean
        get() = false

    override val isScalarList: Boolean
        get() = false

    override val elementType: KClass<*>
        get() = Connection.Edge::class

    override val isElementNullable: Boolean = false

    override fun toString(): String =
        kotlinProp.toString()
}