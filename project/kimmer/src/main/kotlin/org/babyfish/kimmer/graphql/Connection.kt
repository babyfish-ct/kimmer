package org.babyfish.kimmer.graphql

import org.babyfish.kimmer.*

interface Connection<N: Immutable>: Immutable {

    val totalCount: Int

    val edges: List<Edge<N>>

    val pageInfo: PageInfo

    interface Edge<N: Immutable>: Immutable {
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

@Suppress("UNCHECKED_CAST")
fun SyncCreator<Connection.PageInfo>.by(
    base: Connection.PageInfo? = null,
    block: ConnectionDraft.PageInfoDraft.() -> Unit
): Connection.PageInfo =
    produce(type, base, block as (Draft<Connection.PageInfo>) -> Unit)

fun SyncDraftCreator<Connection.PageInfo>.by(
    base: Connection.PageInfo? = null,
    block: ConnectionDraft.PageInfoDraft.() -> Unit
): ConnectionDraft.PageInfoDraft =
    produceDraft(type, base, block)

@Suppress("UNCHECKED_CAST")
suspend fun AsyncCreator<Connection.PageInfo>.by(
    base: Connection.PageInfo? = null,
    block: suspend ConnectionDraft.PageInfoDraft.() -> Unit
): Connection.PageInfo =
    produceAsync(type, base, block as (Draft<Connection.PageInfo>) -> Unit)

suspend fun AsyncDraftCreator<Connection.PageInfo>.by(
    base: Connection.PageInfo? = null,
    block: suspend ConnectionDraft.PageInfoDraft.() -> Unit
): ConnectionDraft.PageInfoDraft =
    produceDraftAsync(type, base, block)