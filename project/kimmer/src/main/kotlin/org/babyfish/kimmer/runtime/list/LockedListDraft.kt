package org.babyfish.kimmer.runtime.list

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.runtime.AsyncDraftContext
import org.babyfish.kimmer.runtime.DraftContext

internal class LockedListDraft<E: Immutable>(
    draftContext: AsyncDraftContext,
    base: List<E>
): LockedList<E?>(
    SimpleListDraft(draftContext, base),
    draftContext
), ListDraft<E> {

    override val draftContext: DraftContext
        get() = (target as ListDraft<*>).draftContext

    override fun resolve(): List<E?> =
        (target as ListDraft<E>).resolve()
}