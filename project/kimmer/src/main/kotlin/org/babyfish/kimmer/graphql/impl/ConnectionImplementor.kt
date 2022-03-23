package org.babyfish.kimmer.graphql.impl

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.jackson.ConnectionSerializer
import org.babyfish.kimmer.runtime.ImmutableSpi

@JsonSerialize(using = ConnectionSerializer::class)
internal abstract class ConnectionImplementor<N: Immutable>(
    private val type: ConnectionType
) : Connection<N>, ImmutableSpi {

    override fun `{type}`(): ConnectionType =
        type
        
    override fun `{get}`(prop: String): Any? =
        when (prop) {
            TOTAL_COUNT-> totalCount
            EDGES -> edges
            PAGE_INFO -> pageInfo
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }
    
    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + if (`{loaded}`(TOTAL_COUNT)) {
            totalCount.hashCode()
        } else {
            0
        }
        hash = 31 * hash + if (`{loaded}`(EDGES)) {
            edges.hashCode()
        } else {
            0
        }
        hash = 31 * hash + if (`{loaded}`(PAGE_INFO)) {
            pageInfo.hashCode()
        } else {
            0
        }
        return hash
    }

    override fun hashCode(shallow: Boolean): Int =
        if (shallow) {
            var hash = 1
            hash = 31 * hash + if (`{loaded}`(TOTAL_COUNT)) {
                totalCount.hashCode()
            } else {
                0
            }
            hash = 31 * hash + if (`{loaded}`(EDGES)) {
                System.identityHashCode(edges)
            } else {
                0
            }
            hash = 31 * hash + if (`{loaded}`(PAGE_INFO)) {
                System.identityHashCode(pageInfo)
            } else {
                0
            }
            hash
        } else {
            hashCode()
        }

    override fun equals(other: Any?): Boolean =
        when {
            this === other ->
                true
            other !is ConnectionImplementor<*> ->
                false
            else ->
                `{loaded}`(TOTAL_COUNT) && other.`{loaded}`(TOTAL_COUNT) &&
                    (!`{loaded}`(TOTAL_COUNT) || totalCount == other.totalCount) &&
                    `{loaded}`(EDGES) && other.`{loaded}`(EDGES) &&
                    (!`{loaded}`(EDGES) || edges == other.edges) &&
                    `{loaded}`(PAGE_INFO) && other.`{loaded}`(PAGE_INFO) &&
                        (!`{loaded}`(PAGE_INFO) || pageInfo == other.pageInfo)
        }

    override fun equals(other: Any?, shallow: Boolean): Boolean =
        if (shallow) {
            when {
                this === other ->
                    true
                other !is ConnectionImplementor<*> ->
                    false
                else ->
                    `{loaded}`(TOTAL_COUNT) && other.`{loaded}`(TOTAL_COUNT) &&
                        (!`{loaded}`(TOTAL_COUNT) || totalCount == other.totalCount) &&
                        `{loaded}`(EDGES) && other.`{loaded}`(EDGES) &&
                        (!`{loaded}`(EDGES) || edges === other.edges) &&
                        `{loaded}`(PAGE_INFO) && other.`{loaded}`(PAGE_INFO) &&
                        (!`{loaded}`(PAGE_INFO) || pageInfo === other.pageInfo)
            }
        } else {
            equals(other)
        }

    override fun toString(): String =
        IMMUTABLE_MAPPER.writeValueAsString(this)
}
