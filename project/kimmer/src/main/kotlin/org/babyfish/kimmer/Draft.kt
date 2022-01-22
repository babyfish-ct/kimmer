package org.babyfish.kimmer

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.DraftSpi
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class DraftDsl

@DraftDsl
interface Draft<out T: Immutable> {

    companion object {

        @JvmStatic
        fun <T: Immutable, V> get(draft: Draft<T>, prop: KProperty1<T, V>): V =
            (draft as DraftSpi).`{get}`(prop.name) as V

        @JvmStatic
        fun get(draft: Draft<*>, prop: ImmutableProp): Any? =
            (draft as DraftSpi).`{get}`(prop.name)

        @JvmStatic
        fun <T: Immutable, V> getOrCreate(draft: Draft<T>, prop: KProperty1<T, V>): V =
            (draft as DraftSpi).`{getOrCreate}`(prop.name) as V

        @JvmStatic
        fun getOrCreate(draft: Draft<*>, prop: ImmutableProp): Any? =
            (draft as DraftSpi).`{getOrCreate}`(prop.name)

        @JvmStatic
        fun <T: Immutable, V> set(draft: Draft<T>, prop: KProperty1<T, V>, value: V) {
            (draft as DraftSpi).`{set}`(prop.name, value)
        }

        @JvmStatic
        fun set(draft: Draft<*>, prop: ImmutableProp, value: Any?) {
            (draft as DraftSpi).`{set}`(prop.name, value)
        }

        @JvmStatic
        fun <T: Immutable> unload(draft: Draft<T>, prop: KProperty1<T, *>) {
            (draft as DraftSpi).`{unload}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> unload(draft: Draft<T>, prop: ImmutableProp) {
            (draft as DraftSpi).`{unload}`(prop.name)
        }
    }
}

interface SyncDraft<out T: Immutable>: Draft<T> {
    fun <X: Immutable> new(type: KClass<X>): SyncDraftCreator<X> =
        SyncDraftCreator(type)
}

interface AsyncDraft<out T: Immutable>: Draft<T> {
    fun <X: Immutable> newAsync(type: KClass<X>) =
        AsyncDraftCreator(type)
}
