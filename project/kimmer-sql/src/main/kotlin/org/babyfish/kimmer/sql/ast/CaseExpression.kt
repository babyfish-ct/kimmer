package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.accept

interface SimpleCaseStartBuilder<C: Any> {

    fun <T: Any> match(
        cond: C,
        value: T
    ): NonNullSimpleCaseBuilder<C, T> =
        match(value(cond), value(value))

    fun <T: Any> match(
        cond: Expression<C>,
        value: T
    ): NonNullSimpleCaseBuilder<C, T> =
        match(cond, value(value))

    fun <T: Any> match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): NonNullSimpleCaseBuilder<C, T>

    fun <T: Any> match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T>
}

interface NonNullSimpleCaseBuilder<C: Any, T: Any> {

    fun match(
        cond: C,
        value: T
    ): NonNullSimpleCaseBuilder<C, T>

    fun match(
        cond: C,
        value: NonNullExpression<T>
    ): NonNullSimpleCaseBuilder<C, T>

    fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): NonNullSimpleCaseBuilder<C, T>

    fun match(
        cond: C,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T>

    fun match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T>

    fun otherwise(
        value: T
    ): NonNullExpression<T> =
        otherwise(value(value))

    fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T>

    fun otherwise(
        expression: Expression<T>
    ): Expression<T>
}

interface NullableSimpleCaseBuilder<C: Any, T: Any> {

    fun match(
        cond: C,
        value: T
    ): NullableSimpleCaseBuilder<C, T>

    fun match(
        cond: C,
        value: NonNullExpression<T>
    ): NullableSimpleCaseBuilder<C, T>

    fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): NullableSimpleCaseBuilder<C, T>

    fun match(
        cond: C,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T>

    fun match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T>

    fun otherwise(value: T): Expression<T>

    fun otherwise(expression: NonNullExpression<T>): Expression<T>

    fun otherwise(expression: Expression<T>): Expression<T>
}

interface CaseStartBuilder {

    fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: T
    ): NonNullCaseBuilder<T> =
        match(cond, value(value))

    fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): NonNullCaseBuilder<T>

    fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: Expression<T>
    ): NullableCaseBuilder<T>
}

interface NonNullCaseBuilder<T: Any> {

    fun match(
        cond: NonNullExpression<Boolean>,
        value: T
    ): NonNullCaseBuilder<T> =
        match(cond, value(value))

    fun match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): NonNullCaseBuilder<T>

    fun match(
        cond: NonNullExpression<Boolean>,
        value: Expression<T>
    ): NullableCaseBuilder<T>

    fun otherwise(
        value: T
    ): NonNullExpression<T> =
        otherwise(value(value))

    fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T>

    fun otherwise(
        expression: Expression<T>
    ): Expression<T>
}

interface NullableCaseBuilder<T: Any> {

    fun match(
        cond: NonNullExpression<Boolean>,
        value: T
    ): NullableCaseBuilder<T> =
        match(cond, value(value))

    fun match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): NullableCaseBuilder<T>

    fun match(
        cond: NonNullExpression<Boolean>,
        value: Expression<T>
    ): NullableCaseBuilder<T>

    fun otherwise(
        value: T
    ): Expression<T> =
        otherwise(value(value))

    fun otherwise(
        expression: NonNullExpression<T>
    ): Expression<T>

    fun otherwise(
        expression: Expression<T>
    ): Expression<T>
}

internal interface CaseChainNode: Renderable, TableReferenceElement

internal class SimpleCaseStartBuilderImpl<C: Any>(
    private val expression: Expression<C>
) : SimpleCaseStartBuilder<C>, CaseChainNode {

    override fun <T: Any> match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): NonNullSimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun <T : Any> match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("case ")
        (expression as Renderable).renderTo(builder)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        expression.accept(visitor)
    }
}

internal class SimpleCaseBuilderImpl<C: Any, T: Any>(
    private val parent: CaseChainNode,
    private val cond: Expression<C>,
    val value: Expression<T>
): NullableSimpleCaseBuilder<C, T>, NonNullSimpleCaseBuilder<C, T>, CaseChainNode {

    init {
        if (parent is SimpleCaseBuilderImpl<*, *>) {
            if (value.selectedType !== parent.value.selectedType) {
                throw IllegalArgumentException(
                    "The value type of branch is different with type of previous branch"
                )
            }
        }
    }

    override fun match(cond: C, value: T): SimpleCaseBuilderImpl<C, T> =
        SimpleCaseBuilderImpl(this, value(cond), value(value))

    override fun match(cond: C, value: NonNullExpression<T>): SimpleCaseBuilderImpl<C, T> =
        SimpleCaseBuilderImpl(this, value(cond), value)

    override fun match(cond: C, value: Expression<T>): SimpleCaseBuilderImpl<C, T> =
        SimpleCaseBuilderImpl(this, value(cond), value)

    override fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilderImpl<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun otherwise(value: T): NonNullExpression<T> =
        CaseExpression(this, value(value))

    override fun otherwise(expression: NonNullExpression<T>): NonNullExpression<T> =
        CaseExpression(this, expression)

    override fun otherwise(expression: Expression<T>): Expression<T> =
        CaseExpression(this, expression)

    override fun renderTo(builder: SqlBuilder) {
        parent.renderTo(builder)
        builder.sql(" when ")
        (cond as Renderable).renderTo(builder)
        builder.sql(" then ")
        (value as Renderable).renderTo(builder)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        parent.accept(visitor)
        cond.accept(visitor)
        value.accept(visitor)
    }
}

internal class CaseStartBuilderImpl: CaseStartBuilder, CaseChainNode {

    override fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): NonNullCaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

    override fun <T : Any> match(
        cond: NonNullExpression<Boolean>,
        value: Expression<T>
    ): NullableCaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("case")
    }

    override fun accept(visitor: TableReferenceVisitor) {}
}

internal class CaseBuilderImpl<T: Any>(
    private val parent: CaseChainNode,
    private val cond: NonNullExpression<Boolean>,
    val value: Expression<T>
): NullableCaseBuilder<T>, NonNullCaseBuilder<T>, CaseChainNode {

    init {
        if (parent is CaseBuilderImpl<*>) {
            if (value.selectedType !== parent.value.selectedType) {
                throw IllegalArgumentException(
                    "The value type of branch is different with type of previous branch"
                )
            }
        }
    }

    override fun match(
        cond: NonNullExpression<Boolean>,
        value: T
    ): CaseBuilderImpl<T> =
        CaseBuilderImpl(this, cond, value(value))

    override fun match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): CaseBuilderImpl<T> =
        CaseBuilderImpl(this, cond, value)

    override fun match(
        cond: NonNullExpression<Boolean>,
        value: Expression<T>
    ): CaseBuilderImpl<T> =
        CaseBuilderImpl(this, cond, value)

    override fun otherwise(value: T): CaseExpression<T> =
        CaseExpression(this, value(value))

    override fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T> =
        CaseExpression(this, expression)

    override fun otherwise(
        expression: Expression<T>
    ): NonNullExpression<T> =
        CaseExpression(this, expression)

    override fun renderTo(builder: SqlBuilder) {
        parent.renderTo(builder)
        builder.sql(" when ")
        (cond as Renderable).renderTo(builder)
        builder.sql(" then ")
        (value as Renderable).renderTo(builder)
    }

    override fun accept(visitor: TableReferenceVisitor) {
        parent.accept(visitor)
        cond.accept(visitor)
        value.accept(visitor)
    }
}

internal class CaseExpression<T: Any>(
    private val parent: CaseChainNode,
    private val otherwise: Expression<T>
): AbstractExpression<T>(when (parent) {
    is SimpleCaseBuilderImpl<*, *> -> parent.value.selectedType
    is CaseBuilderImpl<*> -> parent.value.selectedType
    else -> error("Internal bug, bad parent for case expression")
}) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        lowestPrecedence(false) {
            parent.renderTo(this@render)
            sql(" else ")
            render(otherwise)
            sql(" end")
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        parent.accept(visitor)
        otherwise.accept(visitor)
    }
}