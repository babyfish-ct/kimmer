package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Queries {

    fun <E: Entity<ID>, ID: Comparable<ID>, R> byType(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R>

    fun <S, SID, T, TID, R> byReference(
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
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID>

    fun <S, SID, T, TID, R> byList(
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
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID>

    fun <S, SID, T, TID, R> byConnection(
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
    where S: Entity<SID>, SID: Comparable<SID>, T: Entity<TID>, TID: Comparable<TID>
}