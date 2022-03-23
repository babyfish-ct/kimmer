package org.babyfish.kimmer.graphql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface ConnectionType: ImmutableType {

    val edgeType: ConnectionEdgeType

    val edgesProp: ImmutableProp

    val pageInfoProp: ImmutableProp

    val totalCountProp: ImmutableProp

    companion object {

        @JvmStatic
        fun of(
            nodeType: KClass<out Immutable>
        ): ConnectionType =
            connectionTypeLock.read {
                connectionTypes[nodeType]
            } ?: connectionTypeLock.write {
                connectionTypes[nodeType]
                    ?: createConnectionType(nodeType).also {
                        connectionTypes[nodeType] = it
                    }
            }
    }
}

interface ConnectionEdgeType: ImmutableType {

    val nodeType: ImmutableType

    val nodeProp: ImmutableProp

    val cursorProp: ImmutableProp

    companion object {

        @JvmStatic
        fun of(
            nodeType: KClass<out Immutable>
        ): ConnectionEdgeType =
            ConnectionType.of(nodeType).edgeType
    }
}

private fun createConnectionType(
    nodeType: KClass<out Immutable>
): ConnectionTypeImpl =
    ConnectionTypeImpl(
        ImmutableType.of(nodeType)
    )

private val connectionTypes =
    mutableMapOf<KClass<out Immutable>, ConnectionTypeImpl>()

private val connectionTypeLock =
    ReentrantReadWriteLock()