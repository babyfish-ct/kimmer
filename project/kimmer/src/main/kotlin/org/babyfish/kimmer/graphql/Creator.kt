package org.babyfish.kimmer.graphql

import org.babyfish.kimmer.*
import org.babyfish.kimmer.graphql.impl.*
import org.babyfish.kimmer.graphql.impl.AsyncConnectionDraftImpl
import org.babyfish.kimmer.graphql.impl.ConnectionImplementor
import org.babyfish.kimmer.graphql.impl.EdgeImplementor
import org.babyfish.kimmer.graphql.impl.SyncConnectionDraftImpl
import org.babyfish.kimmer.graphql.impl.SyncEdgeDraftImpl
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.withSyncDraftContext
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun <N: Immutable> produceConnection(
    nodeType: KClass<N>,
    base: Connection<N>? = null,
    block: ConnectionDraft.Sync<N>.() -> Unit
): Connection<N> {
    val connectionType = ConnectionType.of(nodeType)
    return withSyncDraftContext { ctx, isOwner ->
        val draft = SyncConnectionDraftImpl(
            ctx,
            connectionType,
            base as ConnectionImplementor<N>?
        )
        draft.block()
        if (isOwner) {
            draft.`{resolve}`() as Connection<N>
        } else {
            draft
        }
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <N: Immutable> produceConnectionAsync(
    nodeType: KClass<N>,
    base: Connection<N>? = null,
    block: suspend ConnectionDraft.Async<N>.() -> Unit
): Connection<N> {
    val connectionType = ConnectionType.of(nodeType)
    return withAsyncDraftContext { ctx, isOwner ->
        val draft = AsyncConnectionDraftImpl(
            ctx,
            connectionType,
            base as ConnectionImplementor<N>?
        )
        draft.block()
        if (isOwner) {
            draft.`{resolve}`() as Connection<N>
        } else {
            draft
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <N: Immutable> produceConnectionDraft(
    nodeType: KClass<N>,
    base: Connection<N>? = null,
    block: ConnectionDraft.Sync<N>.() -> Unit
): ConnectionDraft.Sync<N> {
    val connectionType = ConnectionType.of(nodeType)
    return withSyncDraftContext(false) { ctx, _ ->
        val draft = SyncConnectionDraftImpl(
            ctx,
            connectionType,
            base as ConnectionImplementor<N>?
        )
        draft.block()
        draft
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <N: Immutable> produceConnectionDraftAsync(
    nodeType: KClass<N>,
    base: Connection<N>? = null,
    block: suspend ConnectionDraft.Async<N>.() -> Unit
): ConnectionDraft.Async<N> {
    val connectionType = ConnectionType.of(nodeType)
    return withAsyncDraftContext(false) { ctx, _ ->
        val draft = AsyncConnectionDraftImpl(
            ctx,
            connectionType,
            base as ConnectionImplementor<N>?
        )
        draft.block()
        draft
    }
}

@Suppress("UNCHECKED_CAST")
fun <N: Immutable> produceEdge(
    nodeType: KClass<N>,
    base: Connection.Edge<N>? = null,
    block: ConnectionDraft.EdgeDraft.Sync<N>.() -> Unit
): Connection.Edge<N> {
    val edgeType = ConnectionEdgeType.of(nodeType)
    return withSyncDraftContext { ctx, isOwner ->
        val draft = SyncEdgeDraftImpl(
            ctx,
            edgeType,
            base as EdgeImplementor<N>?
        )
        draft.block()
        if (isOwner) {
            draft.`{resolve}`() as Connection.Edge<N>
        } else {
            draft
        }
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <N: Immutable> produceEdgeAsync(
    nodeType: KClass<N>,
    base: Connection.Edge<N>? = null,
    block: suspend ConnectionDraft.EdgeDraft.Async<N>.() -> Unit
): Connection.Edge<N> {
    val edgeType = ConnectionEdgeType.of(nodeType)
    return withAsyncDraftContext { ctx, isOwner ->
        val draft = AsyncEdgeDraftImpl(
            ctx,
            edgeType,
            base as EdgeImplementor<N>?
        )
        draft.block()
        if (isOwner) {
            draft.`{resolve}`() as Connection.Edge<N>
        } else {
            draft
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <N: Immutable> produceEdgeDraft(
    nodeType: KClass<N>,
    base: Connection.Edge<N>? = null,
    block: ConnectionDraft.EdgeDraft.Sync<N>.() -> Unit
): ConnectionDraft.EdgeDraft.Sync<N> {
    val edgeType = ConnectionEdgeType.of(nodeType)
    return withSyncDraftContext(false) { ctx, _ ->
        val draft = SyncEdgeDraftImpl(
            ctx,
            edgeType,
            base as EdgeImplementor<N>?
        )
        draft.block()
        draft
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <N: Immutable> produceEdgeDraftAsync(
    nodeType: KClass<N>,
    base: Connection.Edge<N>? = null,
    block: suspend ConnectionDraft.EdgeDraft.Async<N>.() -> Unit
): ConnectionDraft.EdgeDraft.Async<N> {
    val edgeType = ConnectionEdgeType.of(nodeType)
    return withAsyncDraftContext(false) { ctx, _ ->
        val draft = AsyncEdgeDraftImpl(
            ctx,
            edgeType,
            base as EdgeImplementor<N>?
        )
        draft.block()
        draft
    }
}

