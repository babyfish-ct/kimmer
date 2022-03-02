package org.babyfish.kimmer.sql

import org.babyfish.kimmer.SyncCreator
import org.babyfish.kimmer.graphql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface SaveOptions<E: Entity<*>>

inline fun <reified E: Entity<*>> saveOptions(
    noinline block: SaveOptionsDSL<E>.() -> Unit
): SaveOptions<E> =
    saveOptions(E::class, block)

fun <E: Entity<*>> saveOptions(
    type: KClass<E>,
    block: SaveOptionsDSL<E>.() -> Unit
): SaveOptions<E> {
    TODO()
}

interface AbstractSaveOptionsDSL<E: Entity<*>> {

    fun <X: Entity<*>> reference(
        prop: KProperty1<E, X?>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    )

    fun <X: Entity<*>> list(
        prop: KProperty1<E, List<X>>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    )

    fun <X: Entity<*>> connection(
        prop: KProperty1<E, Connection<X>>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    )
}

interface SaveOptionsDSL<E: Entity<*>>: AbstractSaveOptionsDSL<E> {

    fun insertOnly() {}

    fun updateOnly() {}
}

interface AssociatedObjSaveOptionsDSL<E: Entity<*>>: AbstractSaveOptionsDSL<E> {

    fun createAttachingObject(vararg duplicatedProps: KProperty1<E, *>)

    fun deleteDetachedObject()
}
