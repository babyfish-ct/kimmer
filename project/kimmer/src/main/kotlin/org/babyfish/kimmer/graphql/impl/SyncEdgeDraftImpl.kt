package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.*
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType
import org.babyfish.kimmer.runtime.SyncDraftContext
import kotlin.reflect.KClass

internal class SyncEdgeDraftImpl<N: Immutable>(
    draftContext: SyncDraftContext,
    type: ConnectionEdgeType,
    base: EdgeImplementor<N>?
) : EdgeDraftImpl<N>(
    draftContext,
    type,
    base
), ConnectionDraft.EdgeDraft.Sync<N> {

    override fun <X : Immutable> new(type: KClass<X>): SyncDraftCreator<X> =
        SyncDraftCreator(type)

    override val <X : Immutable> MutableList<X>.add: SyncDraftListAdder<X>
        get() = SyncDraftListAdder(this)

    override val <X : Immutable> MutableList<ConnectionDraft.EdgeDraft<X>>.add: SyncEdgeDraftListAdder<X>
        get() = SyncEdgeDraftListAdder(this)
}
