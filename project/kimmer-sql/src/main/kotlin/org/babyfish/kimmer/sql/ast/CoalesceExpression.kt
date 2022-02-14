package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.accept

interface CoalesceBuilder<T: Any> {

    fun or(expression: NonNullExpression<T>): NonNullCoalesceBuilder<T>
    fun or(expression: Expression<T>): CoalesceBuilder<T>
    fun or(value: T): NonNullCoalesceBuilder<T>
    fun end(): Expression<T>
}

interface NonNullCoalesceBuilder<T: Any> : CoalesceBuilder<T> {
    override fun or(expression: Expression<T>): NonNullCoalesceBuilder<T>
    override fun end(): NonNullExpression<T>
}

internal class CoalesceBuilderImpl<T: Any>(
    expression: Expression<T>
): NonNullCoalesceBuilder<T> {

    private val expressions = mutableListOf(expression)

    override fun or(expression: NonNullExpression<T>): NonNullCoalesceBuilder<T> {
        expressions += expression
        return this
    }

    override fun or(expression: Expression<T>): NonNullCoalesceBuilder<T> {
        expressions += expression
        return this
    }

    override fun or(value: T): NonNullCoalesceBuilder<T> {
        expressions += value(value)
        return this
    }

    override fun end(): NonNullExpression<T> =
        CoalesceExpression(expressions)
}

internal class CoalesceExpression<T: Any>(
    private val expressions: List<Expression<T>>
): AbstractExpression<T>(expressions.first().selectedType) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        sql("coalesce(")
        var sp: String? = null
        for (expression in expressions) {
            if (sp === null) {
                sp = ", "
            } else {
                sql(sp)
            }
            render(expression)
        }
        sql(")")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expressions.forEach { it.accept(visitor) }
    }
}