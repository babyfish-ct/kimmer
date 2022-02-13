package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KProperty1

interface SubQueryTable<E: Entity<ID>, ID: Comparable<ID>>: JoinableTable<E, ID> {

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinReference?`(
        prop: KProperty1<E, X?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinList?`(
        prop: KProperty1<E, List<X>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinConnection?`(
        prop: KProperty1<E, Connection<X>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference?`(
        prop: KProperty1<X, E?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList?`(
        prop: KProperty1<X, List<E>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>?>
    ): SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection?`(
        prop: KProperty1<X, Connection<E>?>
    ): SubQueryTable<X, XID>
}