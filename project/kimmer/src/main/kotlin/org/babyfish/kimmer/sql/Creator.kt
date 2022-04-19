package org.babyfish.kimmer.sql

import org.babyfish.kimmer.sql.impl.AssociationImplementor
import org.babyfish.kimmer.sql.impl.AsyncAssociationDraftImpl
import org.babyfish.kimmer.sql.impl.SyncAssociationDraftImpl
import org.babyfish.kimmer.sql.meta.AssociationType
import org.babyfish.kimmer.withAsyncDraftContext
import org.babyfish.kimmer.withSyncDraftContext
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun <S, SID, T, TID> produceAssociation(
    sourceType: KClass<S>,
    targetType: KClass<T>,
    base: Association<S, SID, T, TID>? = null,
    block: AssociationDraft.Sync<S, SID, T, TID>.() -> Unit
): Association<S, SID, T, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {
    val associationType = AssociationType.of(sourceType, targetType)
    return withSyncDraftContext { ctx, isOwner ->
        val draft = SyncAssociationDraftImpl(
            ctx,
            associationType,
            base as AssociationImplementor<S, SID, T, TID>?
        )
        draft.block()
        if (isOwner) {
            draft.`{resolve}`() as Association<S, SID, T, TID>
        } else {
            draft
        }
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <S, SID, T, TID> produceAssociationAsync(
    sourceType: KClass<S>,
    targetType: KClass<T>,
    base: Association<S, SID, T, TID>? = null,
    block: suspend AssociationDraft.Async<S, SID, T, TID>.() -> Unit
): Association<S, SID, T, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {
    val associationType = AssociationType.of(sourceType, targetType)
    return withAsyncDraftContext { ctx, isOwner ->
        val draft = AsyncAssociationDraftImpl(
            ctx,
            associationType,
            base as AssociationImplementor<S, SID, T, TID>?
        )
        draft.block()
        if (isOwner) {
            draft.`{resolve}`() as Association<S, SID, T, TID>
        } else {
            draft
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <S, SID, T, TID> produceAssociationDraft(
    sourceType: KClass<S>,
    targetType: KClass<T>,
    base: Association<S, SID, T, TID>? = null,
    block: AssociationDraft.Sync<S, SID, T, TID>.() -> Unit
): AssociationDraft.Sync<S, SID, T, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {
    val associationType = AssociationType.of(sourceType, targetType)
    return withSyncDraftContext(false) { ctx, _ ->
        val draft = SyncAssociationDraftImpl(
            ctx,
            associationType,
            base as AssociationImplementor<S, SID, T, TID>?
        )
        draft.block()
        draft
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <S, SID, T, TID> produceAssociationDraftAsync(
    sourceType: KClass<S>,
    targetType: KClass<T>,
    base: Association<S, SID, T, TID>? = null,
    block: suspend AssociationDraft.Async<S, SID, T, TID>.() -> Unit
): AssociationDraft.Async<S, SID, T, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {
    val associationType = AssociationType.of(sourceType, targetType)
    return withAsyncDraftContext(false) { ctx, _ ->
        val draft = AsyncAssociationDraftImpl(
            ctx,
            associationType,
            base as AssociationImplementor<S, SID, T, TID>?
        )
        draft.block()
        draft
    }
}