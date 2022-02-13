package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KProperty1

internal class SubQueryTableImpl<E: Entity<ID>, ID: Comparable<ID>>(
    query: AbstractQueryImpl<*, *>,
    entityType: EntityType,
    parent: TableImpl<*, *>? = null,
    isInverse: Boolean = false,
    joinProp: EntityProp? = null,
    isOuterJoin: Boolean = false
) : TableImpl<E, ID>(
    query,
    entityType,
    parent,
    isInverse,
    joinProp,
    isOuterJoin
), SubQueryTable<E, ID> {

    override fun <X: Entity<XID>, XID: Comparable<XID>> createChildTable(
        query: AbstractQueryImpl<*, *>,
        entityType: EntityType,
        isInverse: Boolean,
        joinProp: EntityProp,
        isOuterJoin: Boolean
    ): SubQueryTableImpl<X, XID> =
        SubQueryTableImpl(
            query,
            if (isInverse) joinProp.declaringType else joinProp.targetType!!,
            this,
            isInverse,
            joinProp,
            isOuterJoin
        )

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): SubQueryTable<X, XID> {
        return super.joinReference(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinReference?`(
        prop: KProperty1<E, X?>
    ): SubQueryTable<X, XID> {
        return super.`joinReference?`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>?>
    ): SubQueryTable<X, XID> {
        return super.joinList(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinList?`(
        prop: KProperty1<E, List<X>?>
    ): SubQueryTable<X, XID> {
        return super.`joinList?`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>?>
    ): SubQueryTable<X, XID> {
        return super.joinConnection(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinConnection?`(
        prop: KProperty1<E, Connection<X>?>
    ): SubQueryTable<X, XID> {
        return super.`joinConnection?`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): SubQueryTable<X, XID> {
        return super.`←joinReference`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference?`(
        prop: KProperty1<X, E?>
    ): SubQueryTable<X, XID> {
        return super.`←joinReference?`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>?>
    ): SubQueryTable<X, XID> {
        return super.`←joinList`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList?`(
        prop: KProperty1<X, List<E>?>
    ): SubQueryTable<X, XID> {
        return super.`←joinList?`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>?>
    ): SubQueryTable<X, XID> {
        return super.`←joinConnection`(prop) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection?`(
        prop: KProperty1<X, Connection<E>?>
    ): SubQueryTable<X, XID> {
        return super.`←joinConnection?`(prop) as SubQueryTable<X, XID>
    }
}