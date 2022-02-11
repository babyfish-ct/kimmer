package org.babyfish.kimmer.sql.ast

import java.lang.StringBuilder

internal interface SqlBuilder {

    fun isTableUsed(table: Table<*, *>): Boolean = true

    fun sql(sql: String)

    fun variable(value: Any?)

    fun build(): Pair<String, List<Any?>>
}

internal class JdbcSqlBuilder : SqlBuilder {

    private val builder = StringBuilder()

    private val variables = mutableListOf<Any?>()

    override fun sql(sql: String) {
        builder.append(sql)
    }

    override fun variable(value: Any?) {
        variables += value
        builder.append("?")
    }

    override fun build(): Pair<String, List<Any?>> =
        Pair(builder.toString(), variables.toList())
}

internal class R2dbcSqlBuilder: SqlBuilder {

    private val builder = StringBuilder()

    private val variables = mutableListOf<Any?>()

    override fun sql(sql: String) {
        builder.append(sql)
    }

    override fun variable(value: Any?) {
        variables += value
        builder.append(":")
        builder.append(variables.size)
    }

    override fun build(): Pair<String, List<Any?>> =
        Pair(builder.toString(), variables.toList())
}