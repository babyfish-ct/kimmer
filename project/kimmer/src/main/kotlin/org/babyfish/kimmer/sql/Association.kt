package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.produce
import kotlin.reflect.KClass

interface Association<S, SID, T, TID>: Entity<AssociationId<SID, TID>>
where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {

    val source: S

    val target: T

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <S, SID, T, TID> of(
            source: S,
            target: T
        ): Association<S, SID, T, TID>
        where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> =
            produceAssociation(
                ImmutableType.fromInstance(source).kotlinType as KClass<S>,
                ImmutableType.fromInstance(target).kotlinType as KClass<T>
            ) {
                this.source = source
                this.target = target
            }

        fun <S, SID, T, TID> of(
            sourceType: KClass<S>,
            sourceId: SID,
            targetType: KClass<T>,
            targetId: TID
        ): Association<S, SID, T, TID>
        where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> =
            produceAssociation(sourceType, targetType) {
                this.source = produce(sourceType) {
                    Draft.set(this, Entity<SID>::id, sourceId)
                }
                this.target = produce(targetType) {
                    Draft.set(this, Entity<TID>::id, targetId)
                }
            }
    }
}