package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.accept

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
        cond: C,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value)

    fun <T: Any> match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T>
}

interface SimpleCaseBuilder<C: Any, T: Any> {

    fun match(
        cond: C,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value(value))

    fun match(
        cond: Expression<C>,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(cond, value(value))

    fun match(
        cond: C,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value)

    fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T>

    fun otherwise(
        value: T
    ): NonNullExpression<T> =
        otherwise(value(value))

    fun otherwise(
        expression: NonNullExpression<T>
    ): NonNullExpression<T>
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
}

interface CaseBuilder<T: Any> {

    fun match(
        cond: NonNullExpression<Boolean>,
        value: T
    ): CaseBuilder<T> =
        match(cond, value(value))

    fun match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): CaseBuilder<T>

    fun otherwise(
        value: T
    ): NonNullExpression<T> =
        otherwise(value(value))

    fun otherwise(
        expression: Expression<T>
    ): NonNullExpression<T>
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
    private val value: NonNullExpression<T>
): SimpleCaseBuilder<C, T>, CaseChainNode {

    override fun match(
        cond: Expression<C>,
        value: NonNullExpression<T>
    ): SimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun otherwise(expression: NonNullExpression<T>): NonNullExpression<T> =
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

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("case")
    }

    override fun accept(visitor: TableReferenceVisitor) {}
}

internal class CaseBuilderImpl<T: Any>(
    private val parent: CaseChainNode,
    private val cond: NonNullExpression<Boolean>,
    private val value: NonNullExpression<T>
): CaseBuilder<T>, CaseChainNode {

    override fun match(
        cond: NonNullExpression<Boolean>,
        value: NonNullExpression<T>
    ): CaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

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

internal class CaseExpression<T>(
    private val parent: CaseChainNode,
    private val otherwise: Expression<T>
): AbstractExpression<T>() {

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