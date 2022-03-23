package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.meta.DraftInfo
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass

internal class EdgeTypeImpl(
    override val nodeType: ImmutableType
) : ConnectionEdgeType {

    override val nodeProp: ImmutableProp = EdgeNodePropImpl(this)

    override val cursorProp: ImmutableProp = ScalarPropImpl(this, Connection.Edge<*>::cursor)

    override val kotlinType: KClass<out Immutable>
        get() = Connection.Edge::class

    override val simpleName: String
        get() = Connection.Edge::class.simpleName!!

    override val qualifiedName: String
        get() = Connection.Edge::class.qualifiedName!!

    override val isAbstract: Boolean
        get() = false

    override val superTypes: Set<ImmutableType>
        get() = emptySet()

    override val declaredProps: Map<String, ImmutableProp>
        get() = mapOf(
            "node" to nodeProp,
            "cursor" to cursorProp
        )

    override val props: Map<String, ImmutableProp>
        get() = declaredProps

    override val draftInfo: DraftInfo?
        get() = null

    override fun toString(): String =
        "Connection.Edge<${nodeType.qualifiedName}>"
}