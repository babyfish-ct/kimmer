package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.meta.DraftInfo
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass

internal class ConnectionTypeImpl(
    val nodeType: ImmutableType
): ConnectionType {

    override val edgesProp = ConnectionEdgesPropImpl(this)

    override val pageInfoProp: ImmutableProp = ConnectionPageInfoPropImpl(this)

    override val totalCountProp: ImmutableProp = ScalarPropImpl(this, Connection<*>::totalCount)

    override val edgeType: ConnectionEdgeType
        get() = edgesProp.targetType

    override val kotlinType: KClass<out Immutable>
        get() = Connection::class

    override val simpleName: String
        get() = Connection::class.simpleName!!

    override val qualifiedName: String
        get() = Connection::class.qualifiedName!!

    override val isAbstract: Boolean
        get() = false

    override val superTypes: Set<ImmutableType>
        get() = emptySet()

    override val declaredProps: Map<String, ImmutableProp> =
        mapOf(
            "edges" to edgesProp,
            "pageInfo" to pageInfoProp,
            "totalCount" to totalCountProp
        )

    override val props: Map<String, ImmutableProp>
        get() = declaredProps

    override val draftInfo: DraftInfo?
        get() = null

    override fun toString(): String =
        "Connection<${nodeType.qualifiedName}>"
}