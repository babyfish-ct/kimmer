package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KProperty1

interface JoinableTable<E: Entity<ID>, ID: Comparable<ID>> : Table<E, ID>, Selection<E> {

    fun <X: Entity<XID>, XID: Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X, XID>

    fun <X: Entity<XID>, XID: Comparable<XID>> listContains(
        prop: KProperty1<E, List<X>?>,
        xIds: Collection<XID>
    ): Expression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> connectionContains(
        prop: KProperty1<E, Connection<X>?>,
        xIds: Collection<XID>
    ): Expression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←listContains`(
        prop: KProperty1<X, List<E>?>,
        xIds: Collection<XID>
    ): Expression<Boolean>

    fun <X: Entity<XID>, XID: Comparable<XID>> `←connectionContains`(
        prop: KProperty1<X, Connection<E>?>,
        xIds: Collection<XID>
    ): Expression<Boolean>
}