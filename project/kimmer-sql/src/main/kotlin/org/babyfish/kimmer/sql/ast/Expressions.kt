package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.query.impl.TypedSubQueryImplementor
import org.babyfish.kimmer.sql.ast.query.selectable.AbstractProjection
import org.babyfish.kimmer.sql.ast.query.selectable.Projection
import org.babyfish.kimmer.sql.ast.query.selectable.Projection2
import org.babyfish.kimmer.sql.ast.query.selectable.ProjectionContext
import kotlin.reflect.KClass

fun <X: Any> sql(
    type: KClass<X>,
    sql: String,
    block: (SqlExpressionBuilder.() -> Unit)? = null
): NonNullExpression<X> =
    SqlExpressionBuilder(type).apply {
        if (block !== null) {
            block()
        }
    }.build(sql)


@Suppress("UNCHECKED_CAST")
fun and(
    vararg predicates: NonNullExpression<Boolean>?
): NonNullExpression<Boolean>? =
    combine("and", predicates as Array<NonNullExpression<Boolean>?>)

@Suppress("UNCHECKED_CAST")
fun or(
    vararg predicates: NonNullExpression<Boolean>?
): NonNullExpression<Boolean>? =
    combine("or", predicates as Array<NonNullExpression<Boolean>?>)

private fun combine(
    separator: String,
    predicates: Array<NonNullExpression<Boolean>?>
): NonNullExpression<Boolean>? =
    predicates
        .filterNotNull()
        .takeIf { it.isNotEmpty() }
        ?.let { list ->
            list
                .takeIf { it.size == 1 }
                ?.let { list.first() }
                ?: CombinedExpression(separator, list)
        }


fun not(
    predicate: NonNullExpression<Boolean>?
): NonNullExpression<Boolean>? =
    predicate?.let {
        NotExpression(it)
    }

infix fun Expression<String>.like(
    value: String
): NonNullExpression<Boolean> =
    like(value, LikeMode.ANYWHERE)

fun Expression<String>.like(
    value: String,
    likeMode: LikeMode
): NonNullExpression<Boolean> =
    LikeExpression(this, value, false, likeMode)


infix fun Expression<String>.ilike(
    value: String
): NonNullExpression<Boolean> =
    ilike(value, LikeMode.ANYWHERE)

fun Expression<String>.ilike(
    value: String,
    likeMode: LikeMode
): NonNullExpression<Boolean> =
    LikeExpression(this, value, true, likeMode)


infix fun <T: Comparable<T>> Expression<T>.eq(
    value: T
): NonNullExpression<Boolean> = eq(value(value))

infix fun <T: Comparable<T>> Expression<T>.eq(
    other: Expression<T>
): NonNullExpression<Boolean> =
    ComparisonExpression("=", this, other)


infix fun <T: Comparable<T>> Expression<T>.ne(
    value: T
): NonNullExpression<Boolean> =
    ne(value(value))

infix fun <T: Comparable<T>> Expression<T>.ne(
    other: Expression<T>
): NonNullExpression<Boolean> =
    ComparisonExpression("<>", this, other)


infix fun <T: Comparable<T>> Expression<T>.lt(
    value: T
): NonNullExpression<Boolean> =
    lt(value(value))

infix fun <T: Comparable<T>> Expression<T>.lt(
    other: Expression<T>
): NonNullExpression<Boolean> =
    ComparisonExpression("<", this, other)


infix fun <T: Comparable<T>> Expression<T>.le(
    value: T
): NonNullExpression<Boolean> =
    le(value(value))

infix fun <T: Comparable<T>> Expression<T>.le(
    other: Expression<T>
): NonNullExpression<Boolean> =
    ComparisonExpression("<=", this, other)


infix fun <T: Comparable<T>> Expression<T>.gt(
    value: T
): NonNullExpression<Boolean> =
    gt(value(value))

infix fun <T: Comparable<T>> Expression<T>.gt(
    other: Expression<T>
): NonNullExpression<Boolean> =
    ComparisonExpression(">", this, other)


infix fun <T: Comparable<T>> Expression<T>.ge(
    value: T
): NonNullExpression<Boolean> =
    ge(value(value))

infix fun <T: Comparable<T>> Expression<T>.ge(
    other: Expression<T>
): NonNullExpression<Boolean> =
    ComparisonExpression(">=", this, other)


fun <T: Comparable<T>> Expression<T>.between(
    min: T,
    max: T
): NonNullExpression<Boolean> =
    between(value(min), value(max))

fun <T: Comparable<T>> Expression<T>.between(
    min: Expression<T>,
    max: Expression<T>
): NonNullExpression<Boolean> =
    BetweenExpression(this, min, max)

operator fun <T: Number> NonNullExpression<T>.plus(
    value: T
): NonNullExpression<T> =
    plus(value(value))

operator fun <T: Number> NonNullExpression<T>.plus(
    other: NonNullExpression<T>
): NonNullExpression<T> =
    BinaryExpression("+", this, other)


operator fun <T: Number> Expression<T>.plus(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.plus(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("+", this, other)


operator fun <T: Number> NonNullExpression<T>.minus(
    value: T
): NonNullExpression<T> =
    plus(value(value))

operator fun <T: Number> NonNullExpression<T>.minus(
    other: Expression<T>
): NonNullExpression<T> =
    BinaryExpression("-", this, other)

operator fun <T: Number> Expression<T>.minus(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.minus(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("-", this, other)


operator fun <T: Number> NonNullExpression<T>.times(
    value: T
): NonNullExpression<T> =
    plus(value(value))

operator fun <T: Number> NonNullExpression<T>.times(
    other: Expression<T>
): NonNullExpression<T> =
    BinaryExpression("*", this, other)

operator fun <T: Number> Expression<T>.times(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.times(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("*", this, other)


operator fun <T: Number> NonNullExpression<T>.div(
    value: T
): NonNullExpression<T> =
    plus(value(value))

operator fun <T: Number> NonNullExpression<T>.div(
    other: Expression<T>
): NonNullExpression<T> =
    BinaryExpression("/", this, other)

operator fun <T: Number> Expression<T>.div(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.div(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("/", this, other)


operator fun <T: Number> NonNullExpression<T>.rem(
    value: T
): NonNullExpression<T> =
    plus(value(value))

operator fun <T: Number> NonNullExpression<T>.rem(
    other: Expression<T>
): NonNullExpression<T> =
    BinaryExpression("%", this, other)

operator fun <T: Number> Expression<T>.rem(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.rem(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("%", this, other)


operator fun <T: Number> NonNullExpression<T>.unaryPlus(): NonNullExpression<T> =
    UnaryExpression("+", this)

operator fun <T: Number> Expression<T>.unaryPlus(): Expression<T> =
    UnaryExpression("+", this)


operator fun <T: Number> NonNullExpression<T>.unaryMinus(): NonNullExpression<T> =
    UnaryExpression("-", this)

operator fun <T: Number> Expression<T>.unaryMinus(): Expression<T> =
    UnaryExpression("-", this)


fun Expression<*>.isNull(): NonNullExpression<Boolean> =
    NullityExpression(true, this)

fun Expression<*>.isNotNull(): NonNullExpression<Boolean> =
    NullityExpression(false, this)


fun <T: Any> tuple(
    block: ProjectionContext.() -> Projection<T>
): NonNullExpression<T> =
    (ProjectionContext.block() as AbstractProjection).let {
        TupleExpression(it.selections)
    }


infix fun <T: Any> Expression<T>.valueIn(
    values: Collection<T>
): NonNullExpression<Boolean> =
    InListExpression(false, this, values)

infix fun <T: Any> Expression<T>.valueNotIn(
    values: Collection<T>
): NonNullExpression<Boolean> =
    InListExpression(true, this, values)


infix fun <T: Any> Expression<T>.valueIn(
    subQuery: TypedSubQuery<*, *, *, *, T>
): NonNullExpression<Boolean> =
    InSubQueryExpression(false, this, subQuery)

infix fun <T: Any> Expression<T>.valueNotIn(
    subQuery: TypedSubQuery<*, *, *, *, T>
): NonNullExpression<Boolean> =
    InSubQueryExpression(true, this, subQuery)


fun exists(
    subQuery: MutableSubQuery<*, *, *, *>
): NonNullExpression<Boolean> =
    ExistsExpression(false, subQuery)

fun notExists(
    subQuery: MutableSubQuery<*, *, *, *>
): NonNullExpression<Boolean> =
    ExistsExpression(true, subQuery)


fun exists(
    subQuery: TypedSubQuery<*, *, *, *, *>
): NonNullExpression<Boolean> =
    exists((subQuery as TypedSubQueryImplementor<*, *, *, *, *>).baseQuery)

fun notExists(
    subQuery: TypedSubQuery<*, *, *, *, *>
): NonNullExpression<Boolean> =
    notExists((subQuery as TypedSubQueryImplementor<*, *, *, *, *>).baseQuery)


fun <T: Any> all(
    subQuery: TypedSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("all", subQuery)

fun <T: Any> any(
    subQuery: TypedSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("any", subQuery)

fun <T: Any> some(
    subQuery: TypedSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("some", subQuery)


fun <T: Any> value(value: T): NonNullExpression<T> =
    ValueExpression(value)

fun <T: Any> nullValue(type: KClass<T>): Expression<T> =
    NullValueExpression(type)

fun <T: Number> constant(value: T): NonNullExpression<T> =
    ConstantExpression(value)


fun <C: Any> case(expression: Expression<C>): SimpleCaseStartBuilder<C> =
    SimpleCaseStartBuilderImpl(expression)

fun case(): CaseStartBuilder =
    CaseStartBuilderImpl()


@Suppress("UNCHECKED_CAST")
fun concat(first: NonNullExpression<String>, vararg others: NonNullExpression<String>): NonNullExpression<String> =
    ConcatExpression(first, others as Array<NonNullExpression<String>>)


fun <T: Any> coalesce(expression: NonNullExpression<T>): NonNullCoalesceBuilder<T> =
    CoalesceBuilderImpl(expression)

fun <T: Any> coalesce(expression: Expression<T>): CoalesceBuilder<T> =
    CoalesceBuilderImpl(expression)

fun <T: Any> coalesce(expression: Expression<T>, defaultValue: T): NonNullExpression<T> =
    coalesce(expression).or(defaultValue).end()

fun Expression<*>.count(distinct: Boolean = false): NonNullExpression<Long> =
    AggregationExpression(
        Long::class.java,
        "count",
        this,
        if (distinct) "distinct" else null
    )

fun <T: Number> Expression<T>.min(): Expression<T> =
    AggregationExpression(selectedType, "min", this)

fun <T: Number> Expression<T>.max(): Expression<T> =
    AggregationExpression(selectedType, "max", this)

fun <T: Number> Expression<T>.sum(): Expression<T> =
    AggregationExpression(selectedType, "sum", this)

fun <T: Number> Expression<T>.avg(): Expression<T> =
    AggregationExpression(selectedType, "avg", this)