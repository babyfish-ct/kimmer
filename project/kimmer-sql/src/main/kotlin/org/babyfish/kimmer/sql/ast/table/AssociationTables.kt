package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.impl.AssociationEntityTypeImpl
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> NonNullSubQueryTable<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.source: NonNullSubQueryTable<S, SID>
    where
        S: Entity<SID>,
        T: Entity<TID>,
        SID: Comparable<SID>,
        TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .sourceProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, S>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> SubQueryTable<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.source: SubQueryTable<S, SID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .sourceProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, S>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> NonNullTable<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.source: NonNullTable<S, SID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .sourceProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, S>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> Table<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.source: Table<S, SID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .sourceProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, S>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> NonNullSubQueryTable<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.target: NonNullSubQueryTable<T, TID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .targetProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, T>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> SubQueryTable<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.target: SubQueryTable<T, TID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .targetProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, T>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> NonNullTable<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.target: NonNullTable<T, TID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .targetProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, T>
    )

@Suppress("UNCHECKED_CAST")
val <S, SID, T, TID> Table<
    Association<S, SID, T, TID>,
    AssociationId<SID, TID>
>.target: Table<T, TID>
    where
    S: Entity<SID>,
    T: Entity<TID>,
    SID: Comparable<SID>,
    TID: Comparable<TID>
    get() = joinReference(
        ((this as TableImpl<*, *>).entityType as AssociationEntityTypeImpl)
            .targetProp.immutableProp.kotlinProp
            as KProperty1<Association<S, SID, T, TID>, T>
    )
