package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.ScalarProvider
import java.lang.StringBuilder
import kotlin.reflect.KClass

sealed interface SqlBuilder {
    fun sql(value: String)
    fun variable(value: Any)
    fun nullVariable(type: KClass<*>)
}

internal abstract class AbstractSqlBuilder(
    protected val sqlClient: SqlClient,
    private val parent: AbstractSqlBuilder?,
): SqlBuilder {
    private var childBuilderCount = 0

    private val builder = StringBuilder()

    protected val variables = mutableListOf<Any>()

    private val usedTables: MutableSet<Table<*, *>> =
        parent?.usedTables ?: mutableSetOf()

    // mutableSetOf always is ordered
    private val formulaPropStack: MutableSet<EntityProp> =
        parent?.formulaPropStack ?: mutableSetOf()

    init {
        var p: AbstractSqlBuilder? = parent
        while (p !== null) {
            p.childBuilderCount++
            p = p.parent
        }
    }

    fun useTable(table: TableImpl<*, *>) {
        usedTables += table
        table.parent?.also {
            useTable(it)
        }
    }

    fun isTableUsed(table: Table<*, *>): Boolean =
        usedTables.contains(table)

    override fun sql(sql: String) {
        validate()
        builder.append(sql)
    }

    override fun variable(value: Any) {
        validate()
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
            else ->
                singleVariable(value)
        }
    }
    
    override fun nullVariable(type: KClass<*>) {
        validate()
        val scalarProvider = sqlClient.scalarProviderMap[type]
        onAppendVariable(DbNull(scalarProvider?.sqlType ?: type))
    }

    @Suppress("UNCHECKED_CAST")
    private fun singleVariable(value: Any) {
        if (value is DbNull) {
            throw ExecutionException("Cannot add variable whose type is '${DbNull::class.qualifiedName}'")
        }
        val scalarProvider = sqlClient.scalarProviderMap[value::class] as ScalarProvider<Any, Any>?
        onAppendVariable(scalarProvider?.toSql(value) ?: value)
    }

    fun resolveFormula(formulaProp: EntityProp, block: () -> Unit) {
        validate()
        if (!formulaPropStack.add(formulaProp)) {
            throw MappingException("Failed to resolve formula property '$formulaProp', dead recursion found")
        }
        try {
            block()
        } finally {
            formulaPropStack.remove(formulaProp)
        }
    }

    fun build(
        transformer: ((Pair<String, List<Any>>) -> Pair<String, List<Any>>)? = null
    ): Pair<String, List<Any>> {
        validate()
        val result = builder.toString() to variables
        val transformedResult =
            if (transformer !== null) {
                transformer(result)
            } else {
                result
            }
        var p: AbstractSqlBuilder? = parent
        if (p !== null) {
            p.builder.append(transformedResult.first)
            p.variables.addAll(transformedResult.second)
            while (p !== null) {
                --p.childBuilderCount
                p = p.parent
            }
        }
        return transformedResult
    }

    private fun validate() {
        if (childBuilderCount != 0) {
            error("Internal bug: Cannot change sqlbuilder because there are some child builders")
        }
    }

    protected abstract fun onAppendVariable(value: Any)

    abstract fun createChildBuilder(): AbstractSqlBuilder
}

internal class JdbcSqlBuilder(
    sqlClient: SqlClient,
    parent: JdbcSqlBuilder? = null
) : AbstractSqlBuilder(sqlClient, parent) {

    override fun onAppendVariable(value: Any) {
        variables += value
        sql("?")
    }

    override fun createChildBuilder(): AbstractSqlBuilder =
        JdbcSqlBuilder(sqlClient, this)
}

internal class R2dbcSqlBuilder(
    sqlClient: SqlClient,
    parent: R2dbcSqlBuilder? = null
): AbstractSqlBuilder(sqlClient, parent) {

    override fun onAppendVariable(value: Any) {
        variables += value
        sql(sqlClient.dialect.r2dbcParameter(variables.size))
    }

    override fun createChildBuilder(): AbstractSqlBuilder =
        R2dbcSqlBuilder(sqlClient, this)
}

data class DbNull(val type: KClass<*>)
