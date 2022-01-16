package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.defineClass
import org.springframework.asm.*
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal fun implementationOf(type: Class<out Immutable>): Class<out Immutable> =
    cacheLock.read {
        cacheMap[type]
    } ?: cacheLock.write {
        cacheMap[type]
            ?: ImplementationCreator(cacheMap).let {
                val result = it.create(type)
                cacheMap += it.tmpMap
                result
            }
    }

private class ImplementationCreator(
    private val map: Map<Class<out Immutable>, Class<out Immutable>>
) {
    val tmpMap = mutableMapOf<Class<out Immutable>, Class<out Immutable>?>()

    fun create(modelType: Class<out Immutable>): Class<out Immutable> {
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
        if (!map.containsKey(immutableType.kotlinType.java) && !tmpMap.containsKey(immutableType.kotlinType.java)) {
            createImpl(immutableType)
        }
    }

    private fun createImpl(immutableType: ImmutableType): Class<out Immutable> {
        tmpMap[immutableType.kotlinType.java] = null
        val implementationType = createImplementation(immutableType) as Class<out Immutable>
        tryCreateOtherTypes(immutableType)
        tmpMap[immutableType.kotlinType.java] = implementationType
        return implementationType
    }
}

private val cacheMap = WeakHashMap<Class<out Immutable>, Class<out Immutable>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createImplementation(type: ImmutableType): Class<*> =
    ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            writeType(type)
        }
        .toByteArray()
        .defineClass() as Class<out Immutable>
