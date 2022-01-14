package org.babyfish.kimmer

import org.babyfish.kimmer.jackson.immutableObjectMapper
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.ImmutableSpi
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Immutable {

    companion object {

        @JvmStatic
        fun <T: Immutable> isLoaded(o: T, prop: KProperty1<T, *>): Boolean {
            return (o as ImmutableSpi).`{loaded}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> isLoaded(o: T, prop: ImmutableProp): Boolean {
            return (o as ImmutableSpi).`{loaded}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> get(o: T, prop: KProperty1<T, *>): Any? {
            return (o as ImmutableSpi).`{value}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> get(o: T, prop: ImmutableProp): Any? {
            return (o as ImmutableSpi).`{value}`(prop.name)
        }

        @JvmStatic
        fun <T: Immutable> shallowHashCode(o: T): Int {
            return (o as ImmutableSpi).hashCode(true)
        }

        @JvmStatic
        fun <T: Immutable> shallowEquals(a: T, b: T): Boolean {
            return (a as ImmutableSpi).equals(b, true)
        }

        @JvmStatic
        fun <T: Immutable> fromString(value: String, type: KClass<T>): T =
            fromString(value, type.java)

        @JvmStatic
        fun <T: Immutable> fromString(value: String, type: Class<T>): T =
            objectMapper.readValue(value, type)
    }
}

interface Connection<N>: Immutable {

    val edges: List<Edge<N>>

    val pageInfo: PageInfo

    interface Edge<N>: Immutable {
        val node: N
        val cursor: String
    }

    interface PageInfo: Immutable {
        val hasNextPage: Boolean
        val hasPreviousPage: Boolean
        val startCursor: String
        val endCursor: String
    }
}

@Target(AnnotationTarget.CLASS)
annotation class Abstract

private val objectMapper = immutableObjectMapper()
