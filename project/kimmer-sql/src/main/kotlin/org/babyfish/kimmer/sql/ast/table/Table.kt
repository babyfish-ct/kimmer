package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.NonNullExpression
import org.babyfish.kimmer.sql.ast.PropExpression
import kotlin.reflect.KProperty1

@Suppress("DANGEROUS_CHARACTERS")
interface Table<E: Entity<ID>, ID: Comparable<ID>> {

    val id: PropExpression<ID>

    fun <X: Any> get(
        prop: KProperty1<E, X>
    ): PropExpression<X>

    fun <X: Any> `get?`(
        prop: KProperty1<E, X?>
    ): PropExpression<X>

    fun <X: Entity<XID>, XID: Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): NonNullTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `joinReference?`(
        prop: KProperty1<E, X?>
    ): Table<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>>
    ): NonNullTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `joinList?`(
        prop: KProperty1<E, List<X>>
    ): Table<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>>
    ): NonNullTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `joinConnection?`(
        prop: KProperty1<E, Connection<X>>
    ): Table<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): NonNullTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinReference?`(
        prop: KProperty1<X, E?>
    ): Table<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>>
    ): NonNullTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinList?`(
        prop: KProperty1<X, List<E>>
    ): Table<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>>
    ): NonNullTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinConnection?`(
        prop: KProperty1<X, Connection<E>>
    ): Table<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> listContainsAny(
        prop: KProperty1<E, List<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> connectionContainsAny(
        prop: KProperty1<E, Connection<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> listContainsAll(
        prop: KProperty1<E, List<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> connectionContainsAll(
        prop: KProperty1<E, Connection<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←listContainsAny`(
        prop: KProperty1<X, List<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←connectionContainsAny`(
        prop: KProperty1<X, Connection<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←listContainsAll`(
        prop: KProperty1<X, List<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←connectionContainsAll`(
        prop: KProperty1<X, Connection<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean>
}