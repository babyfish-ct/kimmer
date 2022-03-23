package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.*
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.AsyncDraftContext
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.DraftSpi
import kotlin.reflect.KClass

internal class AsyncEdgeDraftImpl<N: Immutable>(
    draftContext: AsyncDraftContext,
    type: ConnectionEdgeType,
    base: EdgeImplementor<N>?
) : ConnectionDraft.EdgeDraft.Async<N>, DraftSpi {

    private val target = EdgeDraftImpl(
        draftContext,
        type,
        base
    )

    private inline fun <R> lock(block: () -> R): R =
        synchronized(target.`{draftContext}`()) {
            block()
        }

    override fun <X : Immutable> newAsync(type: KClass<X>): AsyncDraftCreator<X> =
        AsyncDraftCreator(type)

    override val <X : Immutable> MutableList<X>.add: AsyncDraftListAdder<X>
        get() = AsyncDraftListAdder(this)

    override val <X : Immutable> MutableList<ConnectionDraft.EdgeDraft<X>>.add: AsyncEdgeDraftListAdder<X>
        get() = AsyncEdgeDraftListAdder(this)

    override fun `{type}`(): ImmutableType =
        target.`{type}`()

    override fun `{draftContext}`(): DraftContext =
        target.`{draftContext}`()

    override var node: N
        get() = lock { target.node }
        set(value) {
            lock { target.node = value }
        }

    override var cursor: String
        get() = lock { target.cursor }
        set(value) {
            lock { target.cursor = value }
        }

    override fun `{loaded}`(prop: String): Boolean =
        lock { target.`{loaded}`(prop) }

    override fun `{get}`(prop: String): Any? =
        lock { target.`{get}`(prop) }

    override fun `{unload}`(prop: String) {
        lock { target.`{unload}`(prop) }
    }

    override fun `{getOrCreate}`(prop: String): Any =
        lock { target.`{getOrCreate}`(prop) }

    override fun `{set}`(prop: String, value: Any?) {
        lock { target.`{set}`(prop, value) }
    }

    override fun `{resolve}`(): Immutable =
        lock { target.`{resolve}`() }

    override fun hashCode(): Int =
        lock { target.hashCode() }

    override fun equals(other: Any?): Boolean =
        lock { target.equals(other, false) }

    override fun hashCode(shallow: Boolean): Int =
        lock { target.hashCode(shallow) }

    override fun equals(other: Any?, shallow: Boolean): Boolean =
        lock { target.equals(other, shallow) }

    override fun toString(): String =
        lock { target.toString() }
}