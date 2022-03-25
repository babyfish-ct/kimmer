package org.babyfish.kimmer.runtime.list

import org.babyfish.kimmer.runtime.DraftContext

internal interface ListDraft<E>: MutableList<E?> {
    val draftContext: DraftContext
    fun resolve(): List<E?>
}