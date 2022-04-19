package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.Queries
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class QueriesImpl(
    private val sqlClient: SqlClientImpl
): Queries {

    override fun <E: Entity<ID>, ID: Comparable<ID>, R> byType(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R> =
        RootMutableQueryImpl(sqlClient, type).run {
            block()
        }

    override fun <S, SID, T, TID, R> byReference(
        prop: KProperty1<S, T?>,
        block: MutableRootQuery<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >.() -> ConfigurableTypedRootQuery<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>,
            R
        >
    ): ConfigurableTypedRootQuery<
        Association<S, SID, T, TID>,
        AssociationId<SID, TID>,
        R
    >
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID> =
        RootMutableQueryImpl<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >(sqlClient, prop).run {
            block()
        }

    override fun <S, SID, T, TID, R> byList(
        prop: KProperty1<S, List<T>>,
        block: MutableRootQuery<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >.() -> ConfigurableTypedRootQuery<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>,
            R
        >
    ): ConfigurableTypedRootQuery<
        Association<S, SID, T, TID>,
        AssociationId<SID, TID>,
        R
    >
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID> =
        RootMutableQueryImpl<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >(sqlClient, prop).run {
            block()
        }

    override fun <S, SID, T, TID, R> byConnection(
        prop: KProperty1<S, Connection<T>>,
        block: MutableRootQuery<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >.() -> ConfigurableTypedRootQuery<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>,
            R
        >
    ): ConfigurableTypedRootQuery<
        Association<S, SID, T, TID>,
        AssociationId<SID, TID>,
        R
    >
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID> =
        RootMutableQueryImpl<
            Association<S, SID, T, TID>,
            AssociationId<SID, TID>
        >(sqlClient, prop).run {
            block()
        }
}