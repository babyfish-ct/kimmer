package org.babyfish.kimmer.sql

import org.babyfish.kimmer.AsyncDraft
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.SyncDraft

interface AssociationDraft<S, SID, T, TID> :
    Association<S,SID, T, TID>,
    Draft<Association<S ,SID, T, TID>>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>
{
    override var source: S

    override var target: T

    interface Sync<S, SID, T, TID>:
        AssociationDraft<S, SID, T, TID>,
        SyncDraft<Association<S, SID, T, TID>>
        where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>

    interface Async<S, SID, T, TID>:
        AssociationDraft<S, SID, T, TID>,
        AsyncDraft<Association<S, SID, T, TID>>
        where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>
}