package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.Abstract
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.defineClass
import org.springframework.asm.*
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal fun draftImplementationOf(modelType: Class<out Immutable>): Class<out Draft<*>> =
    cacheLock.read {
        cacheMap[modelType]
    } ?: cacheLock.write {
        cacheMap[modelType]
            ?: DraftImplementationCreator(cacheMap).let {
                val result = it.create(modelType)
                cacheMap += it.tmpMap
                result
            }
    }

private class DraftImplementationCreator(
    private val map: Map<Class<out Immutable>, Class<out Draft<*>>>
) {
    val tmpMap = mutableMapOf<Class<out Immutable>, Class<out Draft<*>>?>()

    fun create(modelType: Class<out Immutable>): Class<out Draft<*>> {
        if (modelType.isAnnotationPresent(Abstract::class.java)) {
            throw IllegalArgumentException("The immutable type '${modelType.name}' is abstract")
        }
        return createImpl(ImmutableType.of(modelType))
    }

    private fun tryCreateOtherTypes(immutableType: ImmutableType) {
        for (superType in immutableType.superTypes) {
            tryCreate(superType)
        }
        for (prop in immutableType.declaredProps.values) {
            prop.targetType?.let {
                tryCreate(it)
            }
        }
    }

    private fun tryCreate(immutableType: ImmutableType) {
        if (!map.containsKey(immutableType.kotlinType.java) &&
            !tmpMap.containsKey(immutableType.kotlinType.java)) {
            createImpl(immutableType)
        }
    }

    private fun createImpl(immutableType: ImmutableType): Class<out Draft<*>> {
        tmpMap[immutableType.kotlinType.java] = null
        val draftImplementationType = createDraftImplementation(immutableType)
        tryCreateOtherTypes(immutableType)
        tmpMap[immutableType.kotlinType.java] = draftImplementationType
        return draftImplementationType
    }
}

private val cacheMap = WeakHashMap<Class<out Immutable>, Class<out Draft<*>>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createDraftImplementation(immutableType: ImmutableType): Class<out Draft<*>> {
    val draftType = immutableType.draftInfo?.abstractType
    if (draftType !== null && draftType.`package` !== immutableType.kotlinType.java.`package`) {
        throw IllegalArgumentException("Draft type '${draftType.name}' and immutable type ${immutableType.kotlinType.java.name} belongs to different packages")
    }
    return ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            writeType(GeneratorArgs(immutableType))
        }
        .toByteArray()
        .defineClass() as Class<out Draft<*>>
}
