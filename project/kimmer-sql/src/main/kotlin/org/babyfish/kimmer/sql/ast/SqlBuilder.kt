package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import java.lang.StringBuilder

internal abstract class SqlBuilder {

    protected val builder = StringBuilder()

    protected val variables = mutableListOf<Any?>()

    private val usedTables = mutableSetOf<Table<*, *>>()

    private val formulaPropStack = // mutableSetOf always is ordered
        mutableSetOf<EntityProp>()

    fun useTable(table: TableImpl<*, *>) {
        usedTables += table
        table.parent?.also {
            useTable(it)
        }
    }

    fun isTableUsed(table: Table<*, *>): Boolean =
        usedTables.contains(table)

    fun sql(sql: String) {
        builder.append(sql)
    }

    abstract fun variable(value: Any?)

    fun resolveFormula(formulaProp: EntityProp, block: () -> Unit) {
        if (!formulaPropStack.add(formulaProp)) {
            throw MappingException("Failed to resolve formula property '$formulaProp', dead recursion found")
        }
        try {
            block()
        } finally {
            formulaPropStack.remove(formulaProp)
        }
    }

    fun build(): Pair<String, List<Any?>> =
        Pair(builder.toString(), variables.toList())
}

internal class JdbcSqlBuilder : SqlBuilder() {

    override fun variable(value: Any?) {
        variables += value
        builder.append("?")
    }
}

internal class R2dbcSqlBuilder: SqlBuilder() {

    override fun variable(value: Any?) {
        variables += value
        builder.append(":")
        builder.append(variables.size)
    }
}