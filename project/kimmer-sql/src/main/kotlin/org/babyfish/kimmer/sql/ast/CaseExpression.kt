package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.accept

interface SimpleCaseStartBuilder<C: Any> {

    fun <T: Any> match(
        cond: C,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value(value))

    fun <T: Any> match(
        cond: Expression<C>,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(cond, value(value))

    fun <T: Any> match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T>

    fun <T: Any> match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T>
}

interface SimpleCaseBuilder<C: Any, T: Any> {

    fun match(
        cond: C,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value(value))

    fun match(
        cond: C,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value)

    fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T>

    fun match(
        cond: C,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T> =
        match(value(cond), value)

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

interface NullableSimpleCaseBuilder<C: Any, T: Any> : SimpleCaseBuilder<C, T> {

    override fun match(
        cond: C,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value(value))

    override fun match(
        cond: C,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value)

    override fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T>

    @Deprecated(
        "'NullableCaseBuilder.otherwise' returns bad type",
        replaceWith = ReplaceWith("nullableOtherwise")
    )
    override fun otherwise(
        value: T
    ): NonNullExpression<T> =
        otherwise(value(value))

    @Deprecated(
        "'NullableCaseBuilder.otherwise' returns bad type",
        replaceWith = ReplaceWith("nullableOtherwise")
    )
    override fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T>

    fun nullableOtherwise(value: T): Expression<T> =
        nullableOtherwise(value(value))

    fun nullableOtherwise(expression: Expression<T>): Expression<T>
}

interface CaseStartBuilder {

    fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: T
    ): CaseBuilder<T> =
        match(cond, value(value))

    fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): CaseBuilder<T>

    fun <T: Any> match(
        cond: NonNullExpression<Boolean>,
        value: Expression<T>
    ): NullableCaseBuilder<T>
}

interface CaseBuilder<T: Any> {

    fun match(
        cond: Expression<Boolean>,
        value: T
    ): CaseBuilder<T> =
        match(cond, value(value))

    fun match(
        cond: Expression<Boolean>,
        value: NonNullExpression<T>
    ): CaseBuilder<T>

    fun match(
        cond: Expression<Boolean>,
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

interface NullableCaseBuilder<T: Any>: CaseBuilder<T> {

    override fun match(
        cond: Expression<Boolean>,
        value: T
    ): NullableCaseBuilder<T> =
        match(cond, value(value))

    override fun match(
        cond: Expression<Boolean>,
        value: NonNullExpression<T>
    ): NullableCaseBuilder<T>

    @Deprecated(
        "'NullableCaseBuilder.otherwise' returns bad type",
        replaceWith = ReplaceWith("nullableOtherwise")
    )
    override fun otherwise(
        value: T
    ): NonNullExpression<T> =
        otherwise(value(value))

    @Deprecated(
        "'NullableCaseBuilder.otherwise' returns bad type",
        replaceWith = ReplaceWith("nullableOtherwise")
    )
    override fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T>

    fun nullableOtherwise(
        value: T
    ): Expression<T> =
        nullableOtherwise(value(value))

    fun nullableOtherwise(
        expression: NonNullExpression<T>
    ): Expression<T>
}

internal interface CaseChainNode: Renderable, TableReferenceElement

internal class SimpleCaseStartBuilderImpl<C: Any>(
    private val expression: Expression<C>
) : SimpleCaseStartBuilder<C>, CaseChainNode {

    override fun <T: Any> match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
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
): NullableSimpleCaseBuilder<C, T>, CaseChainNode {

    init {
        if (parent is SimpleCaseBuilderImpl<*, *>) {
            if (value.selectedType !== parent.value.selectedType) {
                throw IllegalArgumentException(
                    "The value type of branch is different with type of previous branch"
                )
            }
        }
    }

    override fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun match(
        cond: Expression<C>,
        value: Expression<T>
    ): NullableSimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun otherwise(expression: NonNullExpression<T>): NonNullExpression<T> =
        CaseExpression(this, expression)

    override fun otherwise(expression: Expression<T>): Expression<T> =
        CaseExpression(this, expression)

    override fun nullableOtherwise(expression: Expression<T>): Expression<T> =
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
    ): CaseBuilder<T> =
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
    private val cond: Expression<Boolean>,
    val value: Expression<T>
): NullableCaseBuilder<T>, CaseChainNode {

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
        cond: Expression<Boolean>,
        value: NonNullExpression<T>
    ): NullableCaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

    override fun match(
        cond: Expression<Boolean>,
        value: Expression<T>
    ): NullableCaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

    override fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T> =
        CaseExpression(this, expression)

    override fun otherwise(
        expression: Expression<T>
    ): NonNullExpression<T> =
        CaseExpression(this, expression)

    override fun nullableOtherwise(
        expression: NonNullExpression<T>
    ): Expression<T> =
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