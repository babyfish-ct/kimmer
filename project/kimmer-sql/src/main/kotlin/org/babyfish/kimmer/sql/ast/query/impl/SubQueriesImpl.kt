package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.SubQueries
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class SubQueriesImpl<E: Entity<ID>, ID: Comparable<ID>>(
    private val parentQuery: AbstractMutableQueryImpl<E, ID>
): SubQueries<E, ID> {

    override fun <X, XID, R: Any> byType(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> TypedSubQuery<R>
    ): TypedSubQuery<R>
    where X: Entity<XID>, XID: Comparable<XID> =
        SubMutableQueryImpl(parentQuery, type).run {
            block()
        }

    override fun <S, SID, T, TID, R: Any> byReference(
        prop: KProperty1<S, T?>,
        block: MutableSubQuery<
            E,
            ID,
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >.() -> TypedSubQuery<R>
    ): TypedSubQuery<R>
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID> =
        SubMutableQueryImpl<
            E,
            ID,
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >(parentQuery, prop).run {
            block()
        }

    override fun <S, SID, T, TID, R: Any> byList(
        prop: KProperty1<S, List<T>>,
        block: MutableSubQuery<
            E,
            ID,
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >.() -> TypedSubQuery<R>
    ): TypedSubQuery<R>
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID> =
        SubMutableQueryImpl<
            E,
            ID,
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >(parentQuery, prop).run {
            block()
        }

    override fun <S, SID, T, TID, R: Any> byConnection(
        prop: KProperty1<S, Connection<T>>,
        block: MutableSubQuery<
            E,
            ID,
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >.() -> TypedSubQuery<R>
    ): TypedSubQuery<R>
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID> =
        SubMutableQueryImpl<
            E,
            ID,
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >(parentQuery, prop).run {
            block()
        }
}