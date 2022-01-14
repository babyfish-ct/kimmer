package org.babyfish.kimmer.runtime.list

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.runtime.DraftContext
import kotlin.reflect.full.isSubclassOf

internal class SimpleListDraft<E: Immutable>(
    override val draftContext: DraftContext,
    base: List<E>
): ListProxy<E?>(
    base,
    object: ListElementHandler<E?> {

        override fun input(element: E?) {
            if (element !== null && !element::class.isSubclassOf(Immutable::class)) {
                throw IllegalArgumentException("List element must be instance of '${Immutable::class.qualifiedName}'")
            }
        }

        override fun output(element: E?): E? {
            return draftContext.toDraft(element) as E?
        }

        override fun resolve(element: E?): E {
            return draftContext.resolve(element)!!
        }

        override fun changed(a: E?, b: E?): Boolean {
            return a !== b
        }
    }
), ListDraft<E>