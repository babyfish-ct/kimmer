package org.babyfish.kimmer.sql.ast.table.impl

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.impl.AbstractQueryImpl
import org.babyfish.kimmer.sql.ast.table.NonNullSubQueryTable
import org.babyfish.kimmer.sql.ast.table.SubQueryTable
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KProperty1

internal open class SubQueryTableImpl<E: Entity<ID>, ID: Comparable<ID>>(
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
), NonNullSubQueryTable<E, ID> {

    override fun <X: Entity<XID>, XID: Comparable<XID>> createChildTable(
        query: AbstractQueryImpl<*, *>,
        entityType: EntityType,
        isInverse: Boolean,
        joinProp: EntityProp,
        outerJoin: Boolean
    ): SubQueryTableImpl<X, XID> =
        SubQueryTableImpl(
            query,
            if (isInverse) joinProp.declaringType else joinProp.targetType!!,
            this,
            isInverse,
            joinProp,
            outerJoin
        )

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): NonNullSubQueryTable<X, XID> =
        super.joinReference(prop) as NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinReference?`(
        prop: KProperty1<E, X?>
    ): SubQueryTable<X, XID> =
        super.`joinReference?`(prop) as SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>>
    ): NonNullSubQueryTable<X, XID> =
        super.joinList(prop) as NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinList?`(
        prop: KProperty1<E, List<X>>
    ): SubQueryTable<X, XID> =
        super.`joinList?`(prop) as SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>>
    ): NonNullSubQueryTable<X, XID> =
        super.joinConnection(prop) as NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `joinConnection?`(
        prop: KProperty1<E, Connection<X>>
    ): SubQueryTable<X, XID> =
        super.`joinConnection?`(prop) as SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): NonNullSubQueryTable<X, XID> =
        super.`←joinReference`(prop) as NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinReference?`(
        prop: KProperty1<X, E?>
    ): SubQueryTable<X, XID> =
        super.`←joinReference?`(prop) as SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>>
    ): NonNullSubQueryTable<X, XID> =
        super.`←joinList`(prop) as NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinList?`(
        prop: KProperty1<X, List<E>>
    ): SubQueryTable<X, XID> =
        super.`←joinList?`(prop) as SubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>>
    ): NonNullSubQueryTable<X, XID> =
        super.`←joinConnection`(prop) as NonNullSubQueryTable<X, XID>

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←joinConnection?`(
        prop: KProperty1<X, Connection<E>>
    ): SubQueryTable<X, XID> =
        super.`←joinConnection?`(prop) as SubQueryTable<X, XID>
}