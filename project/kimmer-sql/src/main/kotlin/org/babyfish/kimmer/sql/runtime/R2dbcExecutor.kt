package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Connection
import io.r2dbc.spi.R2dbcException
import io.r2dbc.spi.Result
import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.ast.DbNull

interface R2dbcExecutor {

    suspend fun <R> execute(
        con: Connection,
        sql: String,
        variables: Collection<Any>,
        block: suspend Result.() -> R
    ): R
}

object DefaultR2dbcExecutor: R2dbcExecutor {

    override suspend fun <R> execute(
        con: Connection,
        sql: String,
        variables: Collection<Any>,
        block: suspend Result.() -> R
    ): R {
        val statement = con.createStatement(sql)
        variables.forEachIndexed { index, variable ->
            if (variable is DbNull) {
                statement.bindNull(index, variable.type.java)
            } else {
                statement.bind(index, variable)
            }
        }
        return try {
            statement
                .execute()
                .awaitSingle()
                .block()
        } catch (ex: R2dbcException) {
            throw ExecutionException("Cannot execute SQL [sql: $sql, variables: $variables]", ex)
        }
    }
}


