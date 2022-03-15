package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.RootMutationResult
import org.babyfish.kimmer.sql.ast.Executable

internal class SimpleDeleteCommand(
    private val deleteCommand: DeleteCommand
): Executable<RootMutationResult> {

    override fun execute(con: java.sql.Connection): RootMutationResult =
        deleteCommand.execute(con)[0]

    override suspend fun execute(con: io.r2dbc.spi.Connection): RootMutationResult =
        deleteCommand.execute(con)[0]
}