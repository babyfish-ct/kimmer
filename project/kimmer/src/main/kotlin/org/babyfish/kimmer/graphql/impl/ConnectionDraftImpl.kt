package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.CircularReferenceException
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.DraftSpi

internal open class ConnectionDraftImpl<N: Immutable>(
    private val draftContext: DraftContext,
    type: ConnectionType,
    base: ConnectionImplementor<N>?
) : ConnectionImplementor<N>(type), ConnectionDraft<N>, DraftSpi {

    private val base: ConnectionImplementor<N> = base ?: ConnectionImpl(type)

    private var modified: ConnectionImpl<N>? = null

    private var resolving = false

    private inline val immutable: ConnectionImplementor<N>
        get() = modified ?: base

    private inline val mutable: ConnectionImpl<N>
        get() = modified ?: ConnectionImpl(`{type}`(), base).also {
            modified = it
        }

    override var totalCount: Int
        get() = immutable.totalCount
        set(value) { mutable.setTotalCount(value) }

    @Suppress("UNCHECKED_CAST")
    override var edges: List<Connection.Edge<N>>
        get() = draftContext.toDraft(immutable.edges) as List<ConnectionDraft.EdgeDraft<N>>
        set(value) { mutable.setEdges(value) }

    override var pageInfo: Connection.PageInfo
        get() = draftContext.toDraft(immutable.pageInfo) as Connection.PageInfo
        set(value) { mutable.setPageInfo(value) }

    @Suppress("UNCHECKED_CAST")
    override fun edges(): MutableList<ConnectionDraft.EdgeDraft<N>> {
        if (!immutable.`{loaded}`(EDGES)) {
            mutableListOf<Connection.Edge<N>>().also {
                mutable.setEdges(it)
            }
        }
        return edges as MutableList<ConnectionDraft.EdgeDraft<N>>
    }

    override fun pageInfo(): ConnectionDraft.PageInfoDraft {
        if (!immutable.`{loaded}`(PAGE_INFO)) {
            EMPTY_PAGE_INFO.also {
                mutable.setPageInfo(it)
            }
        }
        return pageInfo as ConnectionDraft.PageInfoDraft
    }

    override fun `{draftContext}`(): DraftContext =
        draftContext

    override fun `{loaded}`(prop: String): Boolean =
        immutable.`{loaded}`(prop)

    @Suppress("UNCHECKED_CAST")
    override fun `{get}`(prop: String): Any? =
        super.`{get}`(prop)?.let {
            when (it) {
                is List<*> -> draftContext.toDraft(it as List<Immutable>)
                is Immutable -> draftContext.toDraft(it)
                else -> it
            }
        }

    override fun `{getOrCreate}`(prop: String): Any =
        when (prop) {
            TOTAL_COUNT ->
                if (immutable.`{loaded}`(TOTAL_COUNT)) {
                    immutable.totalCount
                } else {
                    mutable.setTotalCount(0)
                    0
                }
            EDGES ->
                edges()
            PAGE_INFO ->
                pageInfo()
            else ->
                throw IllegalArgumentException("No such prop '$prop'")
        }

    override fun `{unload}`(prop: String) {
        when (prop) {
            TOTAL_COUNT ->
                if (immutable.`{loaded}`(TOTAL_COUNT)) {
                    mutable.setTotalCount(null)
                }
            EDGES ->
                if (immutable.`{loaded}`(EDGES)) {
                    mutable.setEdges(null)
                }
            PAGE_INFO ->
                if (immutable.`{loaded}`(PAGE_INFO)) {
                    mutable.setPageInfo(null)
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun `{set}`(prop: String, value: Any?) {
        when (prop) {
            TOTAL_COUNT -> mutable.setTotalCount(value as Int)
            EDGES -> mutable.setEdges(value as List<Connection.Edge<N>>)
            PAGE_INFO -> mutable.setPageInfo(value as Connection.PageInfo)
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }
    }

    override fun `{resolve}`(): Immutable {
        if (resolving) {
            throw CircularReferenceException()
        }
        resolving = true
        try {
            if (immutable.`{loaded}`(EDGES)) {
                val unresolved = immutable.edges
                val resolved = draftContext.resolve(unresolved)
                if (unresolved !== resolved) {
                    mutable.setEdges(resolved)
                }
            }
            if (immutable.`{loaded}`(PAGE_INFO)) {
                val unresolved = immutable.pageInfo
                val resolved = draftContext.resolve(unresolved)
                if (unresolved !== resolved) {
                    mutable.setPageInfo(resolved)
                }
            }
            val modified = this.modified
            return if (modified === null || base.equals(modified, true)) {
                base
            } else {
                modified
            }
        } finally {
            resolving = false
        }
    }
}