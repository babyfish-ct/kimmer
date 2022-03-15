package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Connection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.Selection

internal class R2dbcSelector(
    private val sqlClient: SqlClient,
    private val selections: List<Selection<*>>
) {

    suspend fun select(
        con: Connection,
        sql: String,
        variables: List<Any>
    ): List<Any?> =
        sqlClient.r2dbcExecutor.execute(con, sql, variables) {
            mapRows {
                R2dbcResultMapper(sqlClient, this).map(selections)
            }
        }
}
