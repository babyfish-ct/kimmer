package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.ast.DbNull
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass

interface JdbcExecutor {

    fun <R> execute(
        con: Connection,
        sql: String,
        variables: Collection<Any>,
        block: PreparedStatement.() -> R
    ): R
}

object DefaultJdbcExecutor: JdbcExecutor {

    override fun <R> execute(
        con: Connection,
        sql: String,
        variables: Collection<Any>,
        block: PreparedStatement.() -> R
    ): R =
        try {
            con.prepareStatement(sql).use { stmt ->
                variables.forEachIndexed { index, variable ->
                    if (variable is DbNull) {
                        stmt.setNull(index + 1, toJdbcType(variable.type))
                    } else {
                        stmt.setObject(index + 1, variable)
                    }
                }
                stmt.block()
            }
        } catch (ex: SQLException) {
            throw ExecutionException("Cannot execute SQL [sql: $sql, variables: $variables]", ex)
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