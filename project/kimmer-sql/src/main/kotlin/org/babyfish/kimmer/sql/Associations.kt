package org.babyfish.kimmer.sql

import org.babyfish.kimmer.graphql.Connection
import kotlin.reflect.KProperty1

interface Associations {

    fun <S, SID, T, TID> byReference(
        prop: KProperty1<S, T?>
    ): AssociationCommands<SID, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>

    fun <S, SID, T, TID> byList(
        prop: KProperty1<S, List<T>>
    ): AssociationCommands<SID, TID>
        where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>

    fun <S, SID, T, TID> byConnection(
        prop: KProperty1<S, Connection<T>>
    ): AssociationCommands<SID, TID>
        where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>
}