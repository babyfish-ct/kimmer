package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.Selection
import java.sql.Connection

internal class JdbcSelector(
    private val sqlClient: SqlClient,
    private val selections: List<Selection<*>>
) {
    fun select(con: Connection, sql: String, variables: List<Any>): List<Any?> =
        sqlClient.jdbcExecutor.execute(con, sql, variables) {
            mapRows {
                JdbcResultMapper(sqlClient, this).map(selections)
            }
        }
}