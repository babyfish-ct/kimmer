package org.babyfish.kimmer.sql

import org.babyfish.kimmer.sql.ast.Executable

interface AssociationCommands<SID: Comparable<SID>, TID: Comparable<TID>> {

    fun saveCommand(
        sourceId: SID,
        targetId: TID,
        checkExistence: Boolean = true
    ): Executable<EntityMutationResult>

    fun saveCommand(
        idPairs: Collection<Pair<SID, TID>>,
        checkExistence: Boolean = true
    ): Executable<List<EntityMutationResult>>

    fun deleteCommand(
        sourceId: SID,
        targetId: TID
    ): Executable<EntityMutationResult>

    fun deleteCommand(
        idPairs: Collection<Pair<SID, TID>>
    ): Executable<List<EntityMutationResult>>
}