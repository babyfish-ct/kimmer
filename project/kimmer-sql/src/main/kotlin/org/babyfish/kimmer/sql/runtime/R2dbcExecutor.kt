package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Connection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle

typealias R2dbcExecutor = suspend R2dbcExecutorContext.() -> List<Any>

data class R2dbcExecutorContext(
    val connection: Connection,
    val sql: String,
    val variables: List<Any?>
)

val defaultR2dbcExecutor: R2dbcExecutor = {
    defaultImpl()
}

private suspend fun R2dbcExecutorContext.defaultImpl(): List<Any> {
    val statement = connection.createStatement(sql)
    for (index in variables.indices) {
        variables[index]?.let {
            statement.bind(index, it)
        } ?: statement.bindNull(index, String::class.java)
    }
    return statement
        .execute()
        .awaitSingle()
        .map { row, _ -> row }
        .asFlow()
        .toList()
        .also {
            println(it)
        }
}