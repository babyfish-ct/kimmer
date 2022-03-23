package org.babyfish.kimmer.graphql.impl

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType
import org.babyfish.kimmer.jackson.ConnectionEdgeSerializer
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.ImmutableSpi

@JsonSerialize(using = ConnectionEdgeSerializer::class)
internal abstract class EdgeImplementor<N: Immutable>(
    private val type: ConnectionEdgeType
) : Connection.Edge<N>, ImmutableSpi {

    override fun `{type}`(): ConnectionEdgeType =
        type

    override fun `{get}`(prop: String): Any? =
        when (prop) {
            NODE -> node
            CURSOR -> cursor
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }

    override fun hashCode(): Int {
        var hash = 1
        hash = hash * 31 + if (`{loaded}`(NODE)) {
            node.hashCode()
        } else {
            0
        }
        hash = hash * 31 + if (`{loaded}`(CURSOR)) {
            cursor.hashCode()
        } else {
            0
        }
        return hash
    }

    override fun equals(other: Any?): Boolean =
        when {
            this === other ->
                true
            other !is EdgeImplementor<*> ->
                false
            else ->
                `{loaded}`(NODE) == other.`{loaded}`(NODE) &&
                    (!`{loaded}`(NODE) || node == other.node) &&
                    `{loaded}`(CURSOR) == other.`{loaded}`(CURSOR) &&
                    (!`{loaded}`(CURSOR) || cursor == other.cursor)
        }

    override fun hashCode(shallow: Boolean): Int =
        if (shallow) {
            var hash = 1
            hash = hash * 31 + if (`{loaded}`(NODE)) {
                System.identityHashCode(node)
            } else {
                0
            }
            hash = hash * 31 + if (`{loaded}`(CURSOR)) {
                cursor.hashCode()
            } else {
                0
            }
            hash
        } else {
            hashCode()
        }

    override fun equals(other: Any?, shallow: Boolean): Boolean =
        if (shallow) {
            when {
                this === other ->
                    true
                other !is EdgeImplementor<*> ->
                    false
                else ->
                    `{loaded}`(NODE) == other.`{loaded}`(NODE) &&
                        (!`{loaded}`(NODE) || node === other.node) &&
                        `{loaded}`(CURSOR) == other.`{loaded}`(CURSOR) &&
                        (!`{loaded}`(CURSOR) || cursor == other.cursor)
            }
        } else {
            equals(other)
        }

    override fun toString(): String =
        StringBuilder().apply {
            append("{")
            var sp = ""
            if (`{loaded}`(NODE)) {
                append(sp).append("node:").append(node)
                sp = ","
            }
            if (`{loaded}`(CURSOR)) {
                append(sp).append("cursor:").append(cursor)
            }
            append("}")
        }.toString()
}