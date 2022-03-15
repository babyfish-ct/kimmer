package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.RootMutationResult
import org.babyfish.kimmer.sql.ast.Executable

internal class CompositeSaveCommand(
    private val saveCommands: List<SaveCommand>
): Executable<List<RootMutationResult>> {

    override fun execute(
        con: java.sql.Connection
    ): List<RootMutationResult> =
        saveCommands.map { it.execute(con) }

    override suspend fun execute(
        con: io.r2dbc.spi.Connection
    ): List<RootMutationResult> =
        saveCommands.map { it.execute(con) }
}