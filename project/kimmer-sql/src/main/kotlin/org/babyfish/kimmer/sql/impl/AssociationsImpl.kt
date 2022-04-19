package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.AssociationCommands
import org.babyfish.kimmer.sql.Associations
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KProperty1

internal class AssociationsImpl(
    private val sqlClient: SqlClientImpl
): Associations {

    override fun <S, SID, T, TID> byReference(
        prop: KProperty1<S, T?>
    ): AssociationCommands<SID, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> =
        AssociationCommandsImpl(
            sqlClient,
            sqlClient.associationEntityTypeOf(prop)
        )

    override fun <S, SID, T, TID> byList(
        prop: KProperty1<S, List<T>>
    ): AssociationCommands<SID, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> =
        AssociationCommandsImpl(
            sqlClient,
            sqlClient.associationEntityTypeOf(prop)
        )

    override fun <S, SID, T, TID> byConnection(
        prop: KProperty1<S, Connection<T>>
    ): AssociationCommands<SID, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> =
        AssociationCommandsImpl(
            sqlClient,
            sqlClient.associationEntityTypeOf(prop)
        )
}