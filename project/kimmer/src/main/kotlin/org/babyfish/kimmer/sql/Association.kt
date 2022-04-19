package org.babyfish.kimmer.sql

interface Association<S, SID, T, TID>: Entity<AssociationId<SID, TID>>
where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {
    val source: S
    val target: T
}