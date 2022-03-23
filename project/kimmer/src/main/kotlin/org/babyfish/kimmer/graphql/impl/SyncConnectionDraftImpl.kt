package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.SyncEdgeDraftListAdder
import org.babyfish.kimmer.SyncDraftListAdder
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.SyncDraftCreator
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.runtime.SyncDraftContext
import kotlin.reflect.KClass

internal class SyncConnectionDraftImpl<N: Immutable>(
    draftContext: SyncDraftContext,
    type: ConnectionType,
    base: ConnectionImplementor<N>?
) : ConnectionDraftImpl<N>(
    draftContext,
    type,
    base
), ConnectionDraft.Sync<N> {

    override fun <X : Immutable> new(type: KClass<X>): SyncDraftCreator<X> =
        SyncDraftCreator(type)

    override val <X : Immutable> MutableList<X>.add: SyncDraftListAdder<X>
        get() = SyncDraftListAdder(this)

    override val <X : Immutable> MutableList<ConnectionDraft.EdgeDraft<X>>.add: SyncEdgeDraftListAdder<X>
        get() = SyncEdgeDraftListAdder(this)
}