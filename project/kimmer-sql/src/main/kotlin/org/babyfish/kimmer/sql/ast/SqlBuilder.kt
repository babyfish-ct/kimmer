package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import java.lang.StringBuilder
import kotlin.reflect.KClass

internal abstract class SqlBuilder {

    private val builder = StringBuilder()

    protected val variables = mutableListOf<Any>()

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

    fun variable(value: Any) {
        when (value) {
            is Pair<*, *> -> {
                sql("(")
                singleVariable(value.first ?: throw IllegalArgumentException("tuple.first is null"))
                sql(", ")
                singleVariable(value.second ?: throw IllegalArgumentException("tuple.second is null"))
                sql(")")
            }
            is Triple<*, *, *> -> {
                sql("(")
                singleVariable(value.first ?: throw IllegalArgumentException("tuple.first is null"))
                sql(", ")
                singleVariable(value.second ?: throw IllegalArgumentException("tuple.second is null"))
                sql(", ")
                singleVariable(value.third ?: throw IllegalArgumentException("tuple.third is null"))
                sql(")")
            }
            is Tuple4<*, *, *, *> -> {
                sql("(")
                singleVariable(value._1 ?: throw IllegalArgumentException("tuple._1 is null"))
                sql(", ")
                singleVariable(value._2 ?: throw IllegalArgumentException("tuple._2 is null"))
                sql(", ")
                singleVariable(value._3 ?: throw IllegalArgumentException("tuple._3 is null"))
                sql(", ")
                singleVariable(value._4 ?: throw IllegalArgumentException("tuple._4 is null"))
                sql(")")
            }
            is Tuple5<*, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1 ?: throw IllegalArgumentException("tuple._1 is null"))
                sql(", ")
                singleVariable(value._2 ?: throw IllegalArgumentException("tuple._2 is null"))
                sql(", ")
                singleVariable(value._3 ?: throw IllegalArgumentException("tuple._3 is null"))
                sql(", ")
                singleVariable(value._4 ?: throw IllegalArgumentException("tuple._4 is null"))
                sql(", ")
                singleVariable(value._5 ?: throw IllegalArgumentException("tuple._5 is null"))
                sql(")")
            }
            is Tuple6<*, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1 ?: throw IllegalArgumentException("tuple._1 is null"))
                sql(", ")
                singleVariable(value._2 ?: throw IllegalArgumentException("tuple._2 is null"))
                sql(", ")
                singleVariable(value._3 ?: throw IllegalArgumentException("tuple._3 is null"))
                sql(", ")
                singleVariable(value._4 ?: throw IllegalArgumentException("tuple._4 is null"))
                sql(", ")
                singleVariable(value._5 ?: throw IllegalArgumentException("tuple._5 is null"))
                sql(", ")
                singleVariable(value._6 ?: throw IllegalArgumentException("tuple._6 is null"))
                sql(")")
            }
            is Tuple7<*, *, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1 ?: throw IllegalArgumentException("tuple._1 is null"))
                sql(", ")
                singleVariable(value._2 ?: throw IllegalArgumentException("tuple._2 is null"))
                sql(", ")
                singleVariable(value._3 ?: throw IllegalArgumentException("tuple._3 is null"))
                sql(", ")
                singleVariable(value._4 ?: throw IllegalArgumentException("tuple._4 is null"))
                sql(", ")
                singleVariable(value._5 ?: throw IllegalArgumentException("tuple._5 is null"))
                sql(", ")
                singleVariable(value._6 ?: throw IllegalArgumentException("tuple._6 is null"))
                sql(", ")
                singleVariable(value._7 ?: throw IllegalArgumentException("tuple._7 is null"))
                sql(")")
            }
            is Tuple8<*, *, *, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1 ?: throw IllegalArgumentException("tuple._1 is null"))
                sql(", ")
                singleVariable(value._2 ?: throw IllegalArgumentException("tuple._2 is null"))
                sql(", ")
                singleVariable(value._3 ?: throw IllegalArgumentException("tuple._3 is null"))
                sql(", ")
                singleVariable(value._4 ?: throw IllegalArgumentException("tuple._4 is null"))
                sql(", ")
                singleVariable(value._5 ?: throw IllegalArgumentException("tuple._5 is null"))
                sql(", ")
                singleVariable(value._6 ?: throw IllegalArgumentException("tuple._6 is null"))
                sql(", ")
                singleVariable(value._7 ?: throw IllegalArgumentException("tuple._7 is null"))
                sql(", ")
                singleVariable(value._8 ?: throw IllegalArgumentException("tuple._8 is null"))
                sql(")")
            }
            is Tuple9<*, *, *, *, *, *, *, *, *> -> {
                sql("(")
                singleVariable(value._1 ?: throw IllegalArgumentException("tuple._1 is null"))
                sql(", ")
                singleVariable(value._2 ?: throw IllegalArgumentException("tuple._2 is null"))
                sql(", ")
                singleVariable(value._3 ?: throw IllegalArgumentException("tuple._3 is null"))
                sql(", ")
                singleVariable(value._4 ?: throw IllegalArgumentException("tuple._4 is null"))
                sql(", ")
                singleVariable(value._5 ?: throw IllegalArgumentException("tuple._5 is null"))
                sql(", ")
                singleVariable(value._6 ?: throw IllegalArgumentException("tuple._6 is null"))
                sql(", ")
                singleVariable(value._7 ?: throw IllegalArgumentException("tuple._7 is null"))
                sql(", ")
                singleVariable(value._8 ?: throw IllegalArgumentException("tuple._8 is null"))
                sql(", ")
                singleVariable(value._9 ?: throw IllegalArgumentException("tuple._9 is null"))
                sql(")")
            }
            else -> singleVariable(value)
        }
    }

    protected abstract fun singleVariable(value: Any)
    
    fun nullVariable(type: KClass<*>) {
        variables += DbNull(type)
    }

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

    fun build(): Pair<String, List<Any>> =
        builder.toString() to variables
}

internal class JdbcSqlBuilder : SqlBuilder() {

    override fun singleVariable(value: Any) {
        variables += value
        sql("?")
    }
}

internal class R2dbcSqlBuilder: SqlBuilder() {

    override fun singleVariable(value: Any) {
        variables += value
        sql("$")
        sql(variables.size.toString())
    }
}

internal data class DbNull(val type: KClass<*>)
