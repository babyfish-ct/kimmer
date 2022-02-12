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

    fun variable(value: Any?) {
        when (value) {
            is Pair<*, *> -> {
                sql("(")
                singleVariable(value.first)
                sql(", ")
                singleVariable(value.second)
                sql(")")
            }
            is Triple<*, *, *> -> {
                sql("(")
                singleVariable(value.first)
                sql(", ")
                singleVariable(value.second)
                sql(", ")
                singleVariable(value.third)
                sql(")")
            }
            is Tuple4<*, *, *, *> -> {
                sql("(")
                singleVariable(value._1)
                sql(", ")
                singleVariable(value._2)
                sql(", ")
                singleVariable(value._3)
                sql(", ")
                singleVariable(value._4)
                sql(")")
            }
            is Tuple5<*, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1)
                sql(", ")
                singleVariable(value._2)
                sql(", ")
                singleVariable(value._3)
                sql(", ")
                singleVariable(value._4)
                sql(", ")
                singleVariable(value._5)
                sql(")")
            }
            is Tuple6<*, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1)
                sql(", ")
                singleVariable(value._2)
                sql(", ")
                singleVariable(value._3)
                sql(", ")
                singleVariable(value._4)
                sql(", ")
                singleVariable(value._5)
                sql(", ")
                singleVariable(value._6)
                sql(")")
            }
            is Tuple7<*, *, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1)
                sql(", ")
                singleVariable(value._2)
                sql(", ")
                singleVariable(value._3)
                sql(", ")
                singleVariable(value._4)
                sql(", ")
                singleVariable(value._5)
                sql(", ")
                singleVariable(value._6)
                sql(", ")
                singleVariable(value._7)
                sql(")")
            }
            is Tuple8<*, *, *, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1)
                sql(", ")
                singleVariable(value._2)
                sql(", ")
                singleVariable(value._3)
                sql(", ")
                singleVariable(value._4)
                sql(", ")
                singleVariable(value._5)
                sql(", ")
                singleVariable(value._6)
                sql(", ")
                singleVariable(value._7)
                sql(", ")
                singleVariable(value._8)
                sql(")")
            }
            is Tuple9<*, *, *, *, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1)
                sql(", ")
                singleVariable(value._2)
                sql(", ")
                singleVariable(value._3)
                sql(", ")
                singleVariable(value._4)
                sql(", ")
                singleVariable(value._5)
                sql(", ")
                singleVariable(value._6)
                sql(", ")
                singleVariable(value._7)
                sql(", ")
                singleVariable(value._8)
                sql(", ")
                singleVariable(value._9)
                sql(")")
            }
            else -> singleVariable(value)
        }
    }

    protected abstract fun singleVariable(value: Any?)

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

    override fun singleVariable(value: Any?) {
        variables += value
        builder.append("?")
    }
}

internal class R2dbcSqlBuilder: SqlBuilder() {

    override fun singleVariable(value: Any?) {
        variables += value
        builder.append(":")
        builder.append(variables.size)
    }
}