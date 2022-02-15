package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Connection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.kimmer.sql.ast.DbNull
import org.babyfish.kimmer.sql.ast.Selection

typealias R2dbcExecutor = suspend R2dbcExecutorContext.() -> List<Any?>

data class R2dbcExecutorContext(
    val connection: Connection,
    val selections: List<Selection<*>>,
    val sql: String,
    val variables: List<Any>
)

val defaultR2dbcExecutor: R2dbcExecutor = {
    defaultImpl()
}

private suspend fun R2dbcExecutorContext.defaultImpl(): List<Any?> {
    val statement = connection.createStatement(sql)
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
        .map { row, _ ->
            R2dbcResultMapper(row).map(selections)
                ?: Null // Why "asFlow" requires "T: Any"?
        }
        .asFlow()
        .toList()
        .map { if (it === Null) null else it }
}

private object Null
