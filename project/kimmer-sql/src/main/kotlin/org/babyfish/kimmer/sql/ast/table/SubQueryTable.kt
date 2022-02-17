package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KProperty1

interface SubQueryTable<E: Entity<ID>, ID: Comparable<ID>>: Table<E, ID> {

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinReference?`(
        prop: KProperty1<E, X?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>>
    ): NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinList?`(
        prop: KProperty1<E, List<X>>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>>
    ): NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinConnection?`(
        prop: KProperty1<E, Connection<X>>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference?`(
        prop: KProperty1<X, E?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>>
    ): NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList?`(
        prop: KProperty1<X, List<E>>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>>
    ): NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection?`(
        prop: KProperty1<X, Connection<E>>
    ): SubQueryTable<X, XID>
}