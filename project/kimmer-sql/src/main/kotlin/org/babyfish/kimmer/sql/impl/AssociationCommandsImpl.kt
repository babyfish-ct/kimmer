package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.SaveOptionsDSLImpl
import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.meta.impl.AssociationEntityTypeImpl
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal class AssociationCommandsImpl<S, SID, T, TID>(
    private val sqlClient: SqlClientImpl,
    associationEntityTypeImpl: AssociationEntityTypeImpl
) : AssociationCommands<SID, TID>
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {

    private val sourceType = associationEntityTypeImpl.sourceProp.targetType!!.kotlinType as KClass<S>

    private val targetType = associationEntityTypeImpl.targetProp.targetType!!.kotlinType as KClass<T>

    private val mergeOptions = SaveOptionsDSLImpl<Association<S, SID, T, TID>>(
        associationEntityTypeImpl,
        insertable = true,
        updatable = true,
        deletable = false
    ).build()

    private val insertOptions = SaveOptionsDSLImpl<Association<S, SID, T, TID>>(
        associationEntityTypeImpl,
        insertable = true,
        updatable = false,
        deletable = false
    ).build()

    private val deleteOptions = SaveOptionsDSLImpl<Association<S, SID, T, TID>>(
        associationEntityTypeImpl,
        insertable = false,
        updatable = false,
        deletable = true
    ).build()

    override fun saveCommand(
        sourceId: SID,
        targetId: TID,
        checkExistence: Boolean
    ): Executable<EntityMutationResult> =
        SaveCommand(
            sqlClient,
            Association.of(sourceType, sourceId, targetType, targetId),
            if (checkExistence) mergeOptions else insertOptions
        )

    override fun saveCommand(
        idPairs: Collection<Pair<SID, TID>>,
        checkExistence: Boolean
    ): Executable<List<EntityMutationResult>> =
        CompositeSaveCommand(
            idPairs.map {
                SaveCommand(
                    sqlClient,
                    Association.of(sourceType, it.first, targetType, it.second),
                    if (checkExistence) mergeOptions else insertOptions
                )
            }
        )

    override fun deleteCommand(
        sourceId: SID,
        targetId: TID
    ): Executable<EntityMutationResult> =
        SimpleDeleteCommand(
            DeleteCommand(
                sqlClient,
                listOf(sourceId to targetId),
                deleteOptions
            )
        )

    override fun deleteCommand(
        idPairs: Collection<Pair<SID, TID>>
    ): Executable<List<EntityMutationResult>> =
        DeleteCommand(sqlClient, idPairs, deleteOptions)
}