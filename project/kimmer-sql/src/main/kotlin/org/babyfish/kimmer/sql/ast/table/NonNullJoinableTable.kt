package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Selection
import kotlin.reflect.KProperty1

interface NonNullJoinableTable<E: Entity<ID>, ID: Comparable<ID>> :
    JoinableTable<E, ID>, NonNullTable<E, ID>, Selection<E> {

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): NonNullJoinableTable<X, XID>

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>>
    ): NonNullJoinableTable<X, XID>

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>>
    ): NonNullJoinableTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): NonNullJoinableTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>>
    ): NonNullJoinableTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>>
    ): NonNullJoinableTable<X, XID>
}