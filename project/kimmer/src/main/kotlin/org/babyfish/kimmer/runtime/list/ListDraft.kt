package org.babyfish.kimmer.runtime.list

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.runtime.DraftContext

internal interface ListDraft<E: Immutable>: MutableList<E?> {
    val draftContext: DraftContext
    fun resolve(): List<E?>
}