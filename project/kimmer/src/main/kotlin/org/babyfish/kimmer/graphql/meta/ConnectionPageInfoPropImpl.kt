package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.impl.PAGE_INFO
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class ConnectionPageInfoPropImpl(
    override val declaringType: ConnectionTypeImpl
) : ImmutableProp {

    override val name: String
        get() = PAGE_INFO

    override val kotlinProp: KProperty1<*, *>
        get() = Connection<*>::pageInfo

    override val returnType: KClass<*>
        get() = Connection.PageInfo::class

    override val javaReturnType: Class<*>
        get() = returnType.java

    override val targetType: ImmutableType =
        ImmutableType.of(Connection.PageInfo::class)

    override val isNullable: Boolean
        get() = false

    override val isAssociation: Boolean
        get() = true

    override val isReference: Boolean
        get() = true

    override val isList: Boolean
        get() = false

    override val isConnection: Boolean
        get() = false

    override val isTargetNullable: Boolean = false

    override fun toString(): String =
        kotlinProp.toString()
}