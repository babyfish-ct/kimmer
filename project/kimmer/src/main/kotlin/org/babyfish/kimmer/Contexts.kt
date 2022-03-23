package org.babyfish.kimmer

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.babyfish.kimmer.runtime.AsyncDraftContext
import org.babyfish.kimmer.runtime.SyncDraftContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal fun <R> withSyncDraftContext(
    autoCreate: Boolean = true,
    block: (SyncDraftContext, Boolean) -> R
): R {
    val draftContext = draftContextLocal.get()
    return when {
        draftContext !== null ->
            block(draftContext, false)
        autoCreate ->
            SyncDraftContext().let {
                draftContextLocal.set(it)
                try {
                    block(it, true)
                } finally {
                    draftContextLocal.remove()
                }
            }
        else ->
            error("There is no '${SyncDraftContext::class.qualifiedName}' in current thread")
    }
}

internal suspend fun <R> withAsyncDraftContext(
    autoCreate: Boolean = true,
    block: suspend (AsyncDraftContext, Boolean) -> R
): R {
    val draftContext = currentCoroutineContext()[DraftContextElement]?.ctx
    return when {
        draftContext !== null ->
            block(draftContext, false)
        autoCreate ->
            AsyncDraftContext().let {
                withContext(DraftContextElement(it)) {
                    block(it, true)
                }
            }
        else ->
            error("There is no '${AsyncDraftContext::class.qualifiedName}' in current coroutine")
    }
}

private val draftContextLocal = ThreadLocal<SyncDraftContext>()

private data class DraftContextElement(
    val ctx: AsyncDraftContext
) : AbstractCoroutineContextElement(DraftContextElement) {
    companion object Key : CoroutineContext.Key<DraftContextElement>
}