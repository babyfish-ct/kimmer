package org.babyfish.kimmer

import org.babyfish.kimmer.jackson.immutableObjectMapper
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.ImmutableSpi
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * All immutable data types that can be manipulated by kimmer must inherit this interface.
 * This is important because
 *
 * 1. kimmer-ksp can generate source code about mutable draft interfaces for them
 * 2. Their "::class" can be used as the argument of
 *    - [new]
 *    - [newAsync]
 *    - [SyncDraft.new]
 *    - [AsyncDraft.newAsync]
 */
interface Immutable {

    companion object {

        /**
         * Check if a property of an object has already been loaded.
         *
         * Kimmer is designed for server-side development, so dynamism is important.
         *
         * 1. Take GraphQL as example, its data shape is inherently dynamic.
         * 2. Take ORM as example, not all properties(especially associated properties) is always be queried.
         *
         * To support this dynamism, kimmer introduced the concept of "unload property", e.g.
         * ```
         * interface TreeNode: Immutable {
         *     val name: String
         *     val childNodes: List<TreeNode>
         * }
         * val treeNode = new(TreeNode::class).by {
         *     name = "RootNode"
         * }
         * ```
         * Here
         * 1. The user assigned value to name, so name is a loaded field
         * 2. The user did not assign any value to childNodes, so childNodes is an unloaded field
         * ```
         * // Print "true"
         * println(Immutable.isLoaded(treeNode, TreeNode::name))
         *
         * // Print "false"
         * println(Immutable.isLoaded(treeNode, TreeNode::childNodes))
         *
         * // Print "RootNode"
         * println(treeNode.name)
         *
         * // Throw UnloadedException
         * println(treeNode.childNodes)
         * ```
         * As we can see, the unloaded property does not support access by throwing an exception.
         *
         * However, it should be noted that the unloaded property will not cause exception
         * during json serialization, and the unloaded property will be automatically ignored.
         * ```
         * // Explicit json serialization
         * val json1 = immutableObjectMapper().writeValueAsString(treeNode)
         *
         * // Implicit json serialization
         * val json2 = treeNode.toString()
         *
         * // Print "true"
         * println(json1 == json2)
         *
         * // Print { "name": "RootNode" }
         * println(json1)
         * ```
         *
         * @param o The immutable object.
         * @param prop The checked property.
         * @return Whether that property of that object has already been loaded.
         */
        @JvmStatic
        fun <T: Immutable> isLoaded(o: T, prop: KProperty1<T, *>): Boolean {
            return (o as ImmutableSpi).`{loaded}`(prop.name)
        }

        /**
         * Check if a property of an object has already been loaded.
         *
         * Except for a slight difference in the expression of the parameters,
         * the rest of the behavior is no different from the other overloaded function.
         *
         * @param o The immutable object.
         * @param prop The checked property.
         * @return Whether that property of that object has already been loaded.
         */
        @JvmStatic
        fun <T: Immutable> isLoaded(o: T, prop: ImmutableProp): Boolean {
            return (o as ImmutableSpi).`{loaded}`(prop.name)
        }

        /**
         * Access the value of a dynamic property.
         * If that property is unloaded, an exception will be raised
         *
         * @param o The immutable object.
         * @param prop The property to be accessed.
         * @return The property value
         * @exception UnloadedException The specified property is unloaded.
         */
        @JvmStatic
        fun <T: Immutable> get(o: T, prop: KProperty1<T, *>): Any? {
            return (o as ImmutableSpi).`{get}`(prop.name)
        }

        /**
         * Access the value of a dynamic property.
         *
         * Except for a slight difference in the expression of the parameters,
         * the rest of the behavior is no different from the other overloaded function.
         *
         * @param o The immutable object.
         * @param prop The property to be accessed.
         * @return The property value
         * @exception UnloadedException The specified property is unloaded.
         */
        @JvmStatic
        fun <T: Immutable> get(o: T, prop: ImmutableProp): Any? {
            return (o as ImmutableSpi).`{get}`(prop.name)
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

@Target(AnnotationTarget.CLASS)
annotation class Abstract

private val objectMapper = immutableObjectMapper()
