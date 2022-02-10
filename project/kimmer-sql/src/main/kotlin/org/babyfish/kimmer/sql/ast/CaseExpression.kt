package org.babyfish.kimmer.sql.ast

interface SimpleCaseStartBuilder<C> {

    fun <T> match(
        cond: C,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value(value))

    fun <T> match(
        cond: Expression<C>,
        value: T
    ): SimpleCaseBuilder<C, T> =
        match(cond, value(value))

    fun <T> match(
        cond: C,
        value: Expression<T>
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value)

    fun <T> match(
        cond: Expression<C>,
        value: Expression<T>
    ): SimpleCaseBuilder<C, T>
}

interface SimpleCaseBuilder<C, T> {

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
        value: Expression<T>
    ): SimpleCaseBuilder<C, T> =
        match(value(cond), value)

    fun match(
        cond: Expression<C>,
        value: Expression<T>
    ): SimpleCaseBuilder<C, T>

    fun otherwise(
        expression: Expression<T>
    ): Expression<T>
}

interface CaseStartBuilder {

    fun <T> match(
        cond: Expression<Boolean>,
        value: T
    ): CaseBuilder<T> =
        match(cond, value(value))

    fun <T> match(
        cond: Expression<Boolean>,
        value: Expression<T>
    ): CaseBuilder<T>
}

interface CaseBuilder<T> {

    fun match(
        cond: Expression<Boolean>,
        value: T
    ): CaseBuilder<T> =
        match(cond, value(value))

    fun match(
        cond: Expression<Boolean>,
        value: Expression<T>
    ): CaseBuilder<T>

    fun otherwise(
        expression: Expression<T>
    ): Expression<T>
}

internal class SimpleCaseStartBuilderImpl<C>(
    private val expression: Expression<C>
) : SimpleCaseStartBuilder<C>, Renderable {

    override fun <T> match(
        cond: Expression<C>,
        value: Expression<T>
    ): SimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("case ")
        (expression as Renderable).renderTo(builder)
    }
}

internal class SimpleCaseBuilderImpl<C, T>(
    private val parent: Renderable,
    private val cond: Expression<C>,
    private val value: Expression<T>
): SimpleCaseBuilder<C, T>, Renderable {

    override fun match(
        cond: Expression<C>,
        value: Expression<T>
    ): SimpleCaseBuilder<C, T> =
        SimpleCaseBuilderImpl(this, cond, value)

    override fun otherwise(expression: Expression<T>): Expression<T> =
        CaseExpression(this, expression)

    override fun renderTo(builder: SqlBuilder) {
        parent.renderTo(builder)
        builder.sql(" when ")
        (cond as Renderable).renderTo(builder)
        builder.sql(" then ")
        (value as Renderable).renderTo(builder)
    }
}

internal class CaseStartBuilderImpl: CaseStartBuilder, Renderable {

    override fun <T> match(
        cond: Expression<Boolean>,
        value: Expression<T>
    ): CaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("case")
    }
}

internal class CaseBuilderImpl<T>(
    private val parent: Renderable,
    private val cond: Expression<Boolean>,
    private val value: Expression<T>
): CaseBuilder<T>, Renderable {

    override fun match(
        cond: Expression<Boolean>,
        value: Expression<T>
    ): CaseBuilder<T> =
        CaseBuilderImpl(this, cond, value)

    override fun otherwise(
        expression: Expression<T>
    ): Expression<T> =
        CaseExpression(this, expression)

    override fun renderTo(builder: SqlBuilder) {
        parent.renderTo(builder)
        builder.sql(" when ")
        (cond as Renderable).renderTo(builder)
        builder.sql(" then ")
        (value as Renderable).renderTo(builder)
    }
}

internal class CaseExpression<T>(
    private val parent: Renderable,
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
}