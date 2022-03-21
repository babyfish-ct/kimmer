package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.EntityMutationResult
import org.babyfish.kimmer.sql.ast.Executable

internal class CompositeSaveCommand(
    private val saveCommands: List<SaveCommand>
): Executable<List<EntityMutationResult>> {

    override fun execute(
        con: java.sql.Connection
    ): List<EntityMutationResult> =
        saveCommands.map { it.execute(con) }

    override suspend fun execute(
        con: io.r2dbc.spi.Connection
    ): List<EntityMutationResult> =
        saveCommands.map { it.execute(con) }
}