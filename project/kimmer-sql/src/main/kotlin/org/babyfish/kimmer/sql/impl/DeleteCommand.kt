package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.EntityMutationResult
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.runtime.JdbcDeleter
import org.babyfish.kimmer.sql.runtime.MutationOptions
import org.babyfish.kimmer.sql.runtime.R2dbcDeleter
import kotlin.reflect.KClass

internal class DeleteCommand(
    private val sqlClient: SqlClientImpl,
    private val ids: Collection<Any>,
    private val mutationOptions: MutationOptions
): Executable<List<EntityMutationResult>> {

    constructor(
        sqlClient: SqlClientImpl,
        type: KClass<out Entity<*>>,
        ids: Collection<Any>
    ): this(
        sqlClient,
        ids,
        MutationOptions(
            sqlClient.entityTypeMap[type] ?:
            throw IllegalArgumentException(
                "'${type.qualifiedName}' is not mapped entity type of current sqlClient"
            ),
            insertable = false,
            updatable = false,
            deletable = false,
            null,
            mutableMapOf()
        )
    )

    override fun execute(con: java.sql.Connection): List<EntityMutationResult> =
        JdbcDeleter(sqlClient, con).delete(ids, mutationOptions)

    override suspend fun execute(con: io.r2dbc.spi.Connection): List<EntityMutationResult> =
        R2dbcDeleter(sqlClient, con).delete(ids, mutationOptions)
}