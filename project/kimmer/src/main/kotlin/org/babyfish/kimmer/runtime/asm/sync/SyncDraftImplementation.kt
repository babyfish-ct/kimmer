package org.babyfish.kimmer.runtime.asm.sync

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.SyncDraft
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.defineClass
import org.springframework.asm.ClassWriter
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal fun syncDraftImplementationOf(modelType: Class<out Immutable>): Class<out SyncDraft<*>> =
    cacheLock.read {
        cacheMap[modelType]
    } ?: cacheLock.write {
        cacheMap[modelType]
            ?: createSyncDraftImplementation(modelType).let {
                cacheMap[modelType] = it
                it
            }
    }

private val cacheMap = WeakHashMap<Class<out Immutable>, Class<out SyncDraft<*>>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createSyncDraftImplementation(
    modelType: Class<out Immutable>
): Class<out SyncDraft<*>> {
    return ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES).apply {
        writeType(ImmutableType.of(modelType))
    }.toByteArray().defineClass() as Class<out SyncDraft<*>>
}