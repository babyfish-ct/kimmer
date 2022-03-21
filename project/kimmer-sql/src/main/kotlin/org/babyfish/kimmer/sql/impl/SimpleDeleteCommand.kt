package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.EntityMutationResult
import org.babyfish.kimmer.sql.ast.Executable

internal class SimpleDeleteCommand(
    private val deleteCommand: DeleteCommand
): Executable<EntityMutationResult> {

    override fun execute(con: java.sql.Connection): EntityMutationResult =
        deleteCommand.execute(con)[0]

    override suspend fun execute(con: io.r2dbc.spi.Connection): EntityMutationResult =
        deleteCommand.execute(con)[0]
}