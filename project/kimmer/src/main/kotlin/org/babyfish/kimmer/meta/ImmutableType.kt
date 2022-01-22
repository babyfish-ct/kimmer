package org.babyfish.kimmer.meta

import org.babyfish.kimmer.AsyncDraft
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.SyncDraft
import org.babyfish.kimmer.runtime.ImmutableSpi
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface ImmutableType {

    val kotlinType: KClass<out Immutable>

    val simpleName: String
        get() = kotlinType.simpleName!!

    val qualifiedName: String
        get() = kotlinType.qualifiedName!!

    val isAbstract: Boolean

    val superTypes: Set<ImmutableType>

    val declaredProps: Map<String, ImmutableProp>

    val props: Map<String, ImmutableProp>

    val draftInfo: DraftInfo?

    companion object {

        @JvmStatic
        fun of(type: KClass<out Immutable>): ImmutableType =
            getImmutableType(type.java)

        @JvmStatic
        fun of(type: Class<out Immutable>): ImmutableType =
            getImmutableType(type)

        @JvmStatic
        fun fromDraftType(draftType: KClass<out Draft<*>>): ImmutableType =
            fromDraftType(draftType.java)

        @JvmStatic
        fun fromDraftType(draftType: Class<out Draft<*>>): ImmutableType =
            getImmutableTypeByDraftType(draftType)

        @JvmStatic
        fun fromAnyType(type: KClass<*>): ImmutableType? =
            fromAnyType(type.java)

        @JvmStatic
        fun fromInstance(o: Immutable): ImmutableType =
            (o as? ImmutableSpi ?:
            throw IllegalArgumentException(
                "does not accept argument which implements '${Immutable::class.qualifiedName}'" +
                    "but does not implement '${ImmutableSpi::class.qualifiedName}'"
            )
                ).`{type}`()

        @JvmStatic
        fun fromAnyObject(o: Any?): ImmutableType? =
            (o as? ImmutableSpi)?.`{type}`()

        @JvmStatic
        fun fromAnyType(type: Class<*>): ImmutableType? {
            if (Immutable::class.java.isAssignableFrom(type)) {
                if (type.isInterface && !Draft::class.java.isAssignableFrom(type)) {
                    return of(type as Class<out Immutable>)
                }
                val superFromClass = type.superclass?.let {
                    fromAnyType(it)
                }
                if (superFromClass !== null) {
                    return superFromClass
                }
                for (itf in type.interfaces) {
                    val superFromItf = fromAnyType(itf)
                    if (superFromItf !== null) {
                        return superFromItf
                    }
                }
            }
            return null
        }
    }
}

class DraftInfo(
    val abstractType: Class<out Draft<*>>,
    val syncType: Class<out SyncDraft<*>>?,
    val asyncType: Class<out AsyncDraft<*>>?
)

private val cacheMap = WeakHashMap<Class<*>, TypeImpl>()

private val cacheLock = ReentrantReadWriteLock()

private fun getImmutableType(type: Class<out Immutable>): ImmutableType =
    cacheLock.read {
        cacheMap[type]
    } ?: cacheLock.write {
        cacheMap[type]
            ?: Parser(cacheMap).let {
                val result = it.get(type)
                cacheMap += it.terminate()
                result
            }
    }

private fun getImmutableTypeByDraftType(draftType: Class<*>): ImmutableType =
    draftCacheLock.read {
        draftCacheMap[draftType]
    } ?: draftCacheLock.write {
        draftCacheMap[draftType]
            ?: createImmutableTypeByDraftType(draftType)?.also {
                draftCacheMap[draftType] = it
            }
    }

private fun createImmutableTypeByDraftType(draftType: Class<*>): ImmutableType {
    val ctx = DraftScanContext()
    ctx.accept(draftType)
    if (ctx.immutableJavaTypes.isEmpty()) {
        throw IllegalArgumentException("No immutable interface is extended by '${draftType.name}'")
    }
    if (ctx.immutableJavaTypes.size > 1) {
        throw IllegalArgumentException("'${
            draftType.name
        }' extends conflict immutable interfaces: ${
            ctx.immutableJavaTypes.joinToString { it.name }
        }")
    }
    val immutableType = getImmutableType(ctx.immutableJavaTypes[0] as Class<out Immutable>)
    if (immutableType.draftInfo?.abstractType === draftType ||
        immutableType.draftInfo?.syncType === draftType ||
        immutableType.draftInfo?.asyncType === draftType
    ) {
        return immutableType
    }
    throw MetadataException(
        "'${draftType.name}' extends '${immutableType.kotlinType.qualifiedName}'," +
            "but '${immutableType.kotlinType.qualifiedName}' does not consider '${draftType.name}' as its draft type"
    )
}

private val draftCacheMap = WeakHashMap<Class<*>, ImmutableType>()

private val draftCacheLock = ReentrantReadWriteLock()

private class DraftScanContext {

    val immutableJavaTypes = mutableListOf<Class<*>>()

    fun accept(type: Class<*>) {
        if (type.isInterface) {
            if (Draft::class.java.isAssignableFrom(type)) {
                for (superItf in type.interfaces) {
                    accept(superItf)
                }
            } else if (Immutable::class.java.isAssignableFrom(type)) {
                if (Immutable::class.java !== type) {
                    acceptImmutable(type)
                }
                for (superItf in type.interfaces) {
                    accept(superItf)
                }
            }
        }
    }

    private fun acceptImmutable(type: Class<*>) {
        val itr = immutableJavaTypes.iterator()
        while (itr.hasNext()) {
            val existingType = itr.next()
            if (type.isAssignableFrom(existingType)) {
                return
            }
            if (existingType.isAssignableFrom(type)) {
                itr.remove()
            }
        }
        immutableJavaTypes.add(type)
    }
}

