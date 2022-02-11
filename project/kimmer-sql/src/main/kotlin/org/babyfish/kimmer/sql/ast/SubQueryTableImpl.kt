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
    joinType: JoinType = JoinType.INNER
) : TableImpl<E, ID>(
    query,
    entityType,
    parent,
    isInverse,
    joinProp,
    joinType
), SubQueryTable<E, ID> {

    override fun <X: Entity<XID>, XID: Comparable<XID>> createChildTable(
        query: AbstractQueryImpl<*, *>,
        entityType: EntityType,
        isInverse: Boolean,
        joinProp: EntityProp,
        joinType: JoinType
    ): SubQueryTableImpl<X, XID> =
        SubQueryTableImpl(
            query,
            if (isInverse) joinProp.declaringType else joinProp.targetType!!,
            this,
            isInverse,
            joinProp,
            joinType
        )

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>,
        joinType: JoinType
    ): SubQueryTable<X, XID> {
        return super.joinReference(prop, joinType) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>?>,
        joinType: JoinType
    ): SubQueryTable<X, XID> {
        return super.joinList(prop, joinType) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>?>,
        joinType: JoinType
    ): SubQueryTable<X, XID> {
        return super.joinConnection(prop, joinType) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>,
        joinType: JoinType
    ): SubQueryTable<X, XID> {
        return super.`←joinReference`(prop, joinType) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>?>,
        joinType: JoinType
    ): SubQueryTable<X, XID> {
        return super.`←joinList`(prop, joinType) as SubQueryTable<X, XID>
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>?>,
        joinType: JoinType
    ): SubQueryTable<X, XID> {
        return super.`←joinConnection`(prop, joinType) as SubQueryTable<X, XID>
    }
}