package org.babyfish.kimmer.runtime.list

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.runtime.DraftContext
import kotlin.reflect.full.isSubclassOf

internal class SimpleListDraft<E>(
    override val draftContext: DraftContext,
    base: List<E>
): ListProxy<E?>(
    base,
    object: ListElementHandler<E?> {

        override fun input(element: E?) {
            if (element === null) {
                throw IllegalArgumentException("List element cannot be null")
            }
        }

        override fun output(element: E?): E? =
            if (element is Immutable) {
                draftContext.toDraft(element) as E?
            } else {
                element
            }

        override fun resolve(element: E?): E? =
            if (element is Immutable) {
                draftContext.resolve(element)
            } else {
                element
            }

        override fun changed(a: E?, b: E?): Boolean {
            return a !== b
        }
    }
), ListDraft<E>