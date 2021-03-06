package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.runtime.JdbcSaver
import org.babyfish.kimmer.sql.runtime.MutationOptions
import org.babyfish.kimmer.sql.runtime.R2dbcSaver

internal class SaveCommand(
    private val sqlClient: SqlClientImpl,
    private val entity: Entity<*>,
    private val mutationOptions: MutationOptions
): Executable<EntityMutationResult> {

    override fun execute(
        con: java.sql.Connection
    ): EntityMutationResult =
        JdbcSaver(sqlClient, con).save(entity, mutationOptions)

    override suspend fun execute(
        con: io.r2dbc.spi.Connection
    ): EntityMutationResult =
        R2dbcSaver(sqlClient, con).save(entity, mutationOptions)
}