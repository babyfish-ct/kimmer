package org.babyfish.kimmer.graphql

import org.babyfish.kimmer.Immutable

interface Connection<N>: Immutable {

    val edges: List<Edge<N>>

    val pageInfo: PageInfo

    interface Edge<N>: Immutable {
        val node: N
        val cursor: String
    }

    interface PageInfo: Immutable {
        val hasNextPage: Boolean
        val hasPreviousPage: Boolean
        val startCursor: String
        val endCursor: String
    }
}