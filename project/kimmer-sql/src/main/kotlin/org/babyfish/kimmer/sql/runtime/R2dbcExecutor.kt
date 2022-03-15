package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Connection
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.DbNull
import org.babyfish.kimmer.sql.ast.Selection

interface R2dbcExecutor {

    suspend fun <R> execute(
        con: Connection,
        sql: String,
        variables: List<Any>,
        block: suspend Result.() -> R
    ): R
}

object DefaultR2dbcExecutor: R2dbcExecutor {

    override suspend fun <R> execute(
        con: Connection,
        sql: String,
        variables: List<Any>,
        block: suspend Result.() -> R
    ): R {
        val statement = con.createStatement(sql)
        for (index in variables.indices) {
            val variable = variables[index]
            if (variable is DbNull) {
                statement.bindNull(index, variable.type.java)
            } else {
                statement.bind(index, variable)
            }
        }
        return statement
            .execute()
            .awaitSingle()
            .block()
    }
}


