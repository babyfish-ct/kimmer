package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.AsyncDraftCreator
import org.babyfish.kimmer.AsyncDraftListAdder
import org.babyfish.kimmer.AsyncEdgeDraftListAdder
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.runtime.AsyncDraftContext
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.DraftSpi
import org.babyfish.kimmer.sql.AssociationDraft
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.AssociationType
import kotlin.reflect.KClass

internal class AsyncAssociationDraftImpl<S, SID, T, TID>(
    draftContext: AsyncDraftContext,
    type: AssociationType,
    base: AssociationImplementor<S, SID, T, TID>?
) : AssociationDraft.Async<S, SID, T, TID>, DraftSpi
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {
    
    private val sync = AssociationDraftImpl(
        draftContext,
        type,
        base
    )

    private inline fun <R> lock(block: () -> R): R =
        synchronized(sync.`{draftContext}`()) {
            block()
        }

    override fun <X : Immutable> newAsync(type: KClass<X>): AsyncDraftCreator<X> =
        AsyncDraftCreator(type)

    override val <X : Immutable> MutableList<X>.add: AsyncDraftListAdder<X>
        get() = AsyncDraftListAdder(this)

    override val <X : Immutable> MutableList<ConnectionDraft.EdgeDraft<X>>.add: AsyncEdgeDraftListAdder<X>
        get() = AsyncEdgeDraftListAdder(this)

    override fun `{type}`(): AssociationType =
        sync.`{type}`()

    override fun `{draftContext}`(): DraftContext =
        sync.`{draftContext}`()

    override var id: AssociationId<SID, TID>
        get() = lock { sync.id }
        set(value) {
            lock { sync.id = value }
        }

    override var source: S
        get() = lock { sync.source }
        set(value) {
            lock { sync.source = value }
        }

    override var target: T
        get() = lock { sync.target }
        set(value) {
            lock { sync.target = value }
        }

    override fun `{loaded}`(prop: String): Boolean =
        lock { sync.`{loaded}`(prop) }

    override fun `{get}`(prop: String): Any? =
        lock { sync.`{get}`(prop) }

    override fun `{unload}`(prop: String) {
        lock { sync.`{unload}`(prop) }
    }

    override fun `{getOrCreate}`(prop: String): Any =
        lock { sync.`{getOrCreate}`(prop) }

    override fun `{set}`(prop: String, value: Any?) {
        lock { sync.`{set}`(prop, value) }
    }

    override fun `{resolve}`(): Immutable =
        lock { sync.`{resolve}`() }

    override fun hashCode(): Int =
        lock { sync.hashCode() }

    override fun equals(other: Any?): Boolean =
        lock { sync.equals(other, false) }

    override fun hashCode(shallow: Boolean): Int =
        lock { sync.hashCode(shallow) }

    override fun equals(other: Any?, shallow: Boolean): Boolean =
        lock { sync.equals(other, shallow) }

    override fun toString(): String =
        lock { sync.toString() }
}