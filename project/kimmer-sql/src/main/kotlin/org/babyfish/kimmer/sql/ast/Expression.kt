package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import kotlin.reflect.KClass

sealed interface Expression<T> : Selection<T>

internal abstract class AbstractExpression<T>: Expression<T>, Renderable, TableReferenceElement {

    protected abstract fun SqlBuilder.render()

    final override fun renderTo(builder: SqlBuilder) {
        builder.render()
    }

    /*
     * Copy from SQL server documentation
     *
     * 1 ~ (Bitwise NOT)
     * 2 * (Multiplication), / (Division), % (Modulus)
     * 3 + (Positive), - (Negative), + (Addition), + (Concatenation), - (Subtraction), & (Bitwise AND), ^ (Bitwise Exclusive OR), | (Bitwise OR)
     * 4 =, >, <, >=, <=, <>, !=, !>, !< (Comparison operators)
     * 5 NOT
     * 6 AND
     * 7 ALL, ANY, BETWEEN, IN, LIKE, OR, SOME
     * 8 = (Assignment)
     */
    protected abstract val precedence: Int

    protected fun SqlBuilder.render(expression: Expression<*>) {
        (expression as Renderable).let {
            if (it !is AbstractExpression<*> || it.precedence <= precedence) {
                it.renderTo(this)
            } else {
                sql("(")
                it.renderTo(this)
                sql(")")
            }
        }
    }

    protected fun SqlBuilder.lowestPrecedence(
        withBrackets: Boolean,
        block: LowestPrecedenceContext.() -> Unit
    ) {
        if (withBrackets) {
            sql("(")
            LowestPrecedenceContext(this).block()
            sql(")")
        } else {
            LowestPrecedenceContext(this).block()
        }
    }

    protected inner class LowestPrecedenceContext(
        private val builder: SqlBuilder
    ) {
        fun render(expression: Selection<*>) {
            (expression as Renderable).renderTo(builder)
        }
    }
}

internal class PropExpression<T>(
    val table: TableImpl<*, *>,
    val prop: EntityProp
): AbstractExpression<T>() {

    init {
        if (prop.targetType !== null) {
            throw IllegalArgumentException(
                "Can not get '${prop.name}' form table because it's association, " +
                    "please use joinReference, joinList or joinConnection"
            )
        }
        if (prop.storage === null) {
            throw IllegalArgumentException(
                "Can not get '${prop.name}' form table because it's not stored property"
            )
        }
    }

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        table.renderSelection(prop, this, false)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        if (!prop.isId) {
            visitor.visit(table, prop)
        }
    }
}

internal class CombinedExpression(
    private val operator: String,
    private val predicates: List<Expression<*>>
) : AbstractExpression<Boolean>() {

    init {
        predicates
            .takeIf { it.size > 1 }
            ?: error("Internal bug: the size of 'CombinedExpression.expressions' is not greater than 1")
    }

    override val precedence: Int
        get() = if (operator == "and") {
            6
        } else {
            7
        }

    override fun SqlBuilder.render() {
        var sp: String? = null
        for (predicate in predicates) {
            if (sp !== null) {
                sql(sp)
            } else {
                sp = " $operator "
            }
            render(predicate)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        predicates.forEach { it.accept(visitor) }
    }
}

internal class NotExpression(
    private val predicate: Expression<Boolean>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 5

    override fun SqlBuilder.render() {
        sql("not(")
        render(predicate)
        sql(")")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        predicate.accept(visitor)
    }
}

internal class LikeExpression(
    private val expression: Expression<String>,
    pattern: String,
    private val insensitive: Boolean,
    mode: LikeMode
) : AbstractExpression<Boolean>() {

    private val pattern: String? =
        pattern
            .takeIf { it != "" }
            ?.let {
                it
                    .takeIf { !mode.startExact && !it.startsWith("%") }
                    ?: it
            }
            ?.let {
                it
                    .takeIf { !mode.endExact && !it.endsWith("%") }
                    ?: it
            }
            ?.let {
                it
                    .takeIf { insensitive }
                    ?.let { str -> str.lowercase() }
                    ?: it
            }

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        if (pattern === null) {
            sql("1 = 1")
        } else {
            if (insensitive) {
                sql("lower(")
                render(expression)
                sql(")")
            } else {
                render(expression)
            }
            sql(" like ")
            variable(pattern)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
    }
}

internal class ComparisonExpression<T: Comparable<T>>(
    private val operator: String,
    private val left: Expression<T>,
    private val right: Expression<T>
) : AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 4

    override fun SqlBuilder.render() {
        render(left)
        sql(" ")
        sql(operator)
        sql(" ")
        render(right)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        left.accept(visitor)
        right.accept(visitor)
    }
}

internal class BetweenExpression<T: Comparable<T>>(
    private val expression: Expression<T>,
    private val min: Expression<T>,
    private val max: Expression<T>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        render(expression)
        sql(" between ")
        render(min)
        sql(" and ")
        render(max)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
        min.accept(visitor)
        max.accept(visitor)
    }
}

internal class BinaryExpression<T: Number>(
    private val operator: String,
    private val left: Expression<T>,
    private val right: Expression<T>
): AbstractExpression<T>() {

    override val precedence: Int
        get() = when (operator) {
            "*", "/", "%" -> 2
            else -> 3
        }
    override fun SqlBuilder.render() {
        render(left)
        sql(" ")
        sql(operator)
        sql(" ")
        render(right)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        left.accept(visitor)
        right.accept(visitor)
    }
}

internal class UnaryExpression<T: Number>(
    private val operator: String,
    private val target: Expression<T>
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 3

    override fun SqlBuilder.render() {
        sql(operator)
        render(target)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        target.accept(visitor)
    }
}

internal class NullityExpression(
    private val isNull: Boolean,
    private val expression: Expression<*>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        render(expression)
        if (isNull) {
            sql(" is null")
        } else {
            sql(" is not null")
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
    }
}

internal class InListExpression<T>(
    private val negative: Boolean,
    private val expression: Expression<T>,
    private val values: Collection<T>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        if (values.isEmpty()) {
            sql(if (negative) "1 = 1" else "1 = 0")
        } else {
            render(expression)
            sql(if (negative) " not in (" else " in (")
            var separator: String? = null
            for (value in values) {
                if (separator === null) {
                    separator = ", "
                } else {
                    sql(separator)
                }
                variable(value)
            }
            sql(")")
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
    }
}

internal class InSubQueryExpression<T>(
    private val negative: Boolean,
    private val expression: Expression<T>,
    private val subQuery: TypedSqlSubQuery<*, *, *, *, T>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        render(expression)
        sql(if (negative) " not in " else " in ")
        render(subQuery)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
        subQuery.accept(visitor)
    }
}

internal class ExistsExpression(
    private val negative: Boolean,
    private val subQuery: SqlSubQuery<*, *, *, *>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        sql(if (negative) "not " else "")
        sql("exists(select *")
        (subQuery.select(constant(1)) as Renderable).renderTo(this)
        sql(")")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        (subQuery as TableReferenceElement).accept(visitor)
    }
}

internal class OperatorSubQueryExpression<T>(
    private val operator: String,
    private val subQuery: TypedSqlSubQuery<*, *, *, *, T>
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        sql(operator)
        render(subQuery)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        subQuery.accept(visitor)
    }
}

internal class ValueExpression<T>(
    val value: T
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        variable(value)
    }

    override fun accept(visitor: TableReferenceVisitor) {}
}

internal class ConstantExpression<T: Number>(
    private val value: T
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        if (value::class == String::class) {
            error("In order to avoid injection attack, constant expression is not supported for string")
        }
        sql(value.toString())
    }

    override fun accept(visitor: TableReferenceVisitor) {}
}

internal class AggregationExpression<T>(
    private val funName: String,
    private val base: Expression<*>,
    private val prefix: String? = null
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        sql(funName)
        lowestPrecedence(true) {
            prefix?.let {
                sql(it);
                sql(" ")
            }
            render(base)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        base.accept(visitor)
    }
}

internal class ContainsExpression(
    private val table: TableImpl<*, *>,
    prop: EntityProp,
    private val targetIds: Collection<Any>,
    inverse: Boolean
): AbstractExpression<Boolean>() {

    private val tableName: String

    private val targetColumnName: String

    private val thisColumnName: String

    private val thisIdExpression: Expression<*>

    init {
        val middleTable = prop.storage as? MiddleTable
        val column = prop.storage as? Column
        tableName = middleTable?.tableName
            ?: prop.declaringType.tableName

        if (middleTable !== null) {
            if (inverse) {
                targetColumnName = middleTable.joinColumnName
                thisColumnName = middleTable.targetJoinColumnName
            } else {
                targetColumnName = middleTable.targetJoinColumnName
                thisColumnName = middleTable.joinColumnName
            }
        } else {
            targetColumnName = (prop.declaringType.idProp.storage as Column).name
            thisColumnName = column!!.name
        }

        thisIdExpression = table.id
    }

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        if (targetIds.isEmpty()) {
            sql("1 = 0")
            return
        }
        render(thisIdExpression)
        sql(" in (select $thisColumnName from $tableName where $targetColumnName in (")
        var sp: String? = null
        for (targetId in targetIds) {
            if (sp === null) {
                sp = ", "
            } else {
                sql(sp)
            }
            variable(targetId)
        }
        sql("))")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        thisIdExpression.accept(visitor)
    }
}