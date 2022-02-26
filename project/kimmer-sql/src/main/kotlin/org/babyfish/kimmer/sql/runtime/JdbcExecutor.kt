package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.DbNull
import org.babyfish.kimmer.sql.ast.Selection
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Connection
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass

typealias JdbcExecutor = JdbcExecutorContext.() -> List<Any?>

data class JdbcExecutorContext internal constructor(
    val connection: Connection,
    internal val sqlClient: SqlClient,
    internal val selections: List<Selection<*>>,
    val sql: String,
    val variables: List<Any>
)

val defaultJdbcExecutor: JdbcExecutor = {
    defaultImpl()
}

private fun JdbcExecutorContext.defaultImpl(): List<Any?> =
    connection.prepareStatement(sql).use { stmt ->
        for (index in variables.indices) {
            val variable = variables[index]
            if (variable is DbNull) {
                stmt.setNull(index + 1, toJdbcType(variable.type))
            } else {
                stmt.setObject(index + 1, variable)
            }
        }
        stmt.executeQuery().use { rs ->
            val rows = mutableListOf<Any?>()
            while (rs.next()) {
                rows += JdbcResultMapper(sqlClient, rs).map(selections)
            }
            rows
        }
    }

private fun toJdbcType(type: KClass<*>): Int =
    when (type) {
        Boolean::class -> Types.BOOLEAN
        Char::class -> Types.CHAR
        Byte::class -> Types.TINYINT
        Short::class -> Types.SMALLINT
        Int::class -> Types.INTEGER
        Long::class -> Types.BIGINT
        Float::class -> Types.FLOAT
        Double::class -> Types.DOUBLE
        BigInteger::class -> Types.BIGINT
        BigDecimal::class -> Types.DECIMAL
        String::class -> Types.VARCHAR
        UUID::class -> Types.VARCHAR
        LocalDate::class -> Types.DATE
        LocalDateTime::class -> Types.DATE
        else -> throw IllegalArgumentException("Cannot convert '$type' to java.sql.Types")
    }