package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.*
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.DraftSpi

internal open class EdgeDraftImpl<N: Immutable>(
    private val draftContext: DraftContext,
    type: ConnectionEdgeType,
    base: EdgeImplementor<N>?
) : EdgeImplementor<N>(type), ConnectionDraft.EdgeDraft<N>, DraftSpi {

    private val base: EdgeImplementor<N> = base ?: EdgeImpl(type)

    private var modified: EdgeImpl<N>? = null

    private var resolving = false

    private inline val immutable: EdgeImplementor<N>
        get() = modified ?: base

    private inline val mutable: EdgeImpl<N>
        get() = modified ?: EdgeImpl<N>(`{type}`(), base).also {
            modified = it
        }

    @Suppress("UNCHECKED_CAST")
    override var node: N
        get() = draftContext.toDraft(immutable.node) as N
        set(value) { mutable.setNode(value) }

    override var cursor: String
        get() = immutable.cursor
        set(value) { mutable.setCursor(value) }

    @Suppress("UNCHECKED_CAST")
    private fun node(): Draft<N> {
        if (!immutable.`{loaded}`(NODE)) {
            produce(`{type}`().nodeType.kotlinType) {}.also {
                mutable.setNode(it as N)
            }
        }
        return node as Draft<N>
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
            NODE -> node()
            CURSOR ->
                if (immutable.`{loaded}`(CURSOR)) {
                    immutable.cursor
                } else {
                    mutable.setCursor("")
                    ""
                }
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }

    override fun `{unload}`(prop: String) {
        when (prop) {
            NODE -> if (immutable.`{loaded}`(NODE)) {
                mutable.setNode(null)
            }
            CURSOR -> if (immutable.`{loaded}`(CURSOR)) {
                mutable.setCursor(null)
            }
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }
    }

    override fun `{set}`(prop: String, value: Any?) {
        when (prop) {
            NODE -> mutable.setNode(
                (value ?: throw IllegalArgumentException("Unexpected null")) as N
            )
            CURSOR -> mutable.setCursor(prop)
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }
    }

    override fun `{resolve}`(): Immutable {
        if (resolving) {
            throw CircularReferenceException()
        }
        resolving = true
        try {
            if (immutable.`{loaded}`(NODE)) {
                val unresolved = immutable.node
                val resolved = draftContext.resolve(unresolved)
                if (unresolved !== resolved) {
                    mutable.setNode(resolved)
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