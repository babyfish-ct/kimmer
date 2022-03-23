package org.babyfish.kimmer.graphql

import org.babyfish.kimmer.AsyncDraft
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.SyncDraft

interface ConnectionDraft<N: Immutable> : Connection<N>, Draft<Connection<N>> {

    override var edges: List<Connection.Edge<N>>
    override var pageInfo: Connection.PageInfo
    override var totalCount: Int
    fun edges(): MutableList<EdgeDraft<N>>
    fun pageInfo(): PageInfoDraft

    interface EdgeDraft<N: Immutable>: Connection.Edge<N>, Draft<Connection.Edge<N>> {

        override var node: N
        override var cursor: String

        interface Sync<N: Immutable>: EdgeDraft<N>, SyncDraft<Connection.Edge<N>>
        interface Async<N: Immutable>: EdgeDraft<N>, AsyncDraft<Connection.Edge<N>>
    }

    interface PageInfoDraft: Connection.PageInfo, Draft<Connection.PageInfo> {

        override var hasNextPage: Boolean
        override var hasPreviousPage: Boolean
        override var startCursor: String
        override var endCursor: String

        interface Sync: PageInfoDraft, SyncDraft<Connection.PageInfo>
        interface Async: PageInfoDraft, AsyncDraft<Connection.PageInfo>
    }

    interface Sync<N: Immutable>: ConnectionDraft<N>, SyncDraft<Connection<N>>
    interface Async<N: Immutable>: ConnectionDraft<N>, AsyncDraft<Connection<N>>
}
