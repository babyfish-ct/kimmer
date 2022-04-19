package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.SyncDraftCreator
import org.babyfish.kimmer.SyncDraftListAdder
import org.babyfish.kimmer.SyncEdgeDraftListAdder
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.runtime.SyncDraftContext
import org.babyfish.kimmer.sql.AssociationDraft
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.AssociationType
import kotlin.reflect.KClass

internal class SyncAssociationDraftImpl<S, SID, T, TID>(
    draftContext: SyncDraftContext,
    type: AssociationType,
    base: AssociationImplementor<S, SID, T, TID>?
) : AssociationDraftImpl<S, SID, T, TID>(
    draftContext,
    type,
    base
), AssociationDraft.Sync<S, SID, T, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {

    override fun <X : Immutable> new(type: KClass<X>): SyncDraftCreator<X> =
        SyncDraftCreator(type)

    override val <X : Immutable> MutableList<X>.add: SyncDraftListAdder<X>
        get() = SyncDraftListAdder(this)

    override val <X : Immutable> MutableList<ConnectionDraft.EdgeDraft<X>>.add: SyncEdgeDraftListAdder<X>
        get() = SyncEdgeDraftListAdder(this)
}