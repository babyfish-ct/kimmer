package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.accept
import kotlin.reflect.KClass

class SqlExpressionBuilder internal constructor(
    private val type: KClass<*>
) {

    private var expressions: List<Expression<*>> = emptyList()

    private var values: List<Any> = emptyList()

    fun expressions(vararg expressions: Expression<*>) {
        this.expressions = expressions.toList()
    }

    fun values(vararg values: Any) {
        this.values = values.toList()
    }

    internal fun <T: Any> build(sql: String): NonNullExpression<T> {

        if (sql.indexOf('\'') != -1) {
            throw IllegalArgumentException("SQL template cannot contains \"'\"")
        }

        val sqlLen = sql.length
        val parts = mutableListOf<Any>()
        var index = 0
        var usedExpressionCount = 0
        var usedValueCount = 0
        while (true) {
            val newIndex = sql.indexOf('%', index)
            if (newIndex == -1) {
                break
            }
            if (newIndex > index) {
                parts += sql.substring(index, newIndex)
            }
            val partType =
                if (newIndex + 1 < sqlLen) {
                    sql[newIndex + 1]
                } else {
                    ' '
                }
            when (partType) {
                'e' -> {
                    if (usedExpressionCount > expressions.size) {
                        throw IllegalArgumentException("No enough expressions")
                    }
                    parts += expressions[usedExpressionCount++]
                }
                'v' -> {
                    if (usedExpressionCount > values.size) {
                        throw IllegalArgumentException("No enough values")
                    }
                    parts += value(values[usedValueCount++])
                }
                else -> throw IllegalArgumentException(
                    "Illegal SQL template '$sql', position: %$newIndex, only '%e' and '%v' are supported"
                )
            }
            index = newIndex + 2
        }
        if (usedExpressionCount < expressions.size) {
            throw IllegalArgumentException("Too many expression")
        }
        if (usedValueCount < values.size) {
            throw IllegalArgumentException("Too many values")
        }
        if (index + 1 < sqlLen) {
            parts += sql.substring(index)
        }
        return SqlExpression(type.java, parts)
    }
}

internal class SqlExpression<T: Any>(
    selectedType: Class<*>,
    private val parts: List<Any>
): AbstractExpression<T>(selectedType) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        lowestPrecedence(false) {
            for (part in parts) {
                if (part is Expression<*>) {
                    render(part)
                } else {
                    sql(part as String)
                }
            }
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        parts.forEach { (it as? Expression<*>)?.accept(visitor) }
    }
}
