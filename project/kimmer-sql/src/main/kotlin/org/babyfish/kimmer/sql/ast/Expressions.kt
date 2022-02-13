package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.query.impl.TypedSubQueryImplementor
import kotlin.reflect.KClass

fun <X: Any> sql(
    type: KClass<X>,
    sql: String,
    block: (SqlExpressionBuilder.() -> Unit)? = null
): NonNullExpression<X> =
    SqlExpressionBuilder().apply {
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

fun <A, B> tuple(
    a: Selection<A>,
    b: Selection<B>
): NonNullExpression<Pair<A, B>> =
    PairExpression(a, b)

fun <A, B, C> tuple(
    a: Selection<A>,
    b: Selection<B>,
    c: Selection<C>
): NonNullExpression<Triple<A, B, C>> =
    TripleExpression(a, b, c)

fun <T1, T2, T3, T4> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
): NonNullExpression<Tuple4<T1, T2, T3, T4>> =
    Tuple4Expression(selection1, selection2, selection3, selection4)

fun <T1, T2, T3, T4, T5> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
): NonNullExpression<Tuple5<T1, T2, T3, T4, T5>> =
    Tuple5Expression(selection1, selection2, selection3, selection4, selection5)

fun <T1, T2, T3, T4, T5, T6> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
): NonNullExpression<Tuple6<T1, T2, T3, T4, T5, T6>> =
    Tuple6Expression(selection1, selection2, selection3, selection4, selection5, selection6)

fun <T1, T2, T3, T4, T5, T6, T7> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
    selection7: Selection<T7>,
): NonNullExpression<Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
    Tuple7Expression(selection1, selection2, selection3, selection4, selection5, selection6, selection7)

fun <T1, T2, T3, T4, T5, T6, T7, T8> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
    selection7: Selection<T7>,
    selection8: Selection<T8>,
): NonNullExpression<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
    Tuple8Expression(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
    selection7: Selection<T7>,
    selection8: Selection<T8>,
    selection9: Selection<T9>,
): NonNullExpression<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
    Tuple9Expression(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9)


infix fun <T> Expression<T>.valueIn(
    values: Collection<T>
): NonNullExpression<Boolean> =
    InListExpression(false, this, values)

infix fun <T> Expression<T>.valueNotIn(
    values: Collection<T>
): NonNullExpression<Boolean> =
    InListExpression(true, this, values)


infix fun <T> Expression<T>.valueIn(
    subQuery: TypedSubQuery<*, *, *, *, T>
): NonNullExpression<Boolean> =
    InSubQueryExpression(false, this, subQuery)

infix fun <T> Expression<T>.valueNotIn(
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
    exists((subQuery as TypedSubQueryImplementor<*, *, *, *>).baseQuery)

fun notExists(
    subQuery: TypedSubQuery<*, *, *, *, *>
): NonNullExpression<Boolean> =
    notExists((subQuery as TypedSubQueryImplementor<*, *, *, *>).baseQuery)


fun <T> all(
    subQuery: TypedSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("all", subQuery)

fun <T> any(
    subQuery: TypedSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("any", subQuery)

fun <T> some(
    subQuery: TypedSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("some", subQuery)

fun <T: Any> value(value: T): NonNullExpression<T> =
    ValueExpression(value)

fun <T: Number> constant(value: T): NonNullExpression<T> =
    ConstantExpression(value)

fun <C: Any> case(expression: Expression<C>): SimpleCaseStartBuilder<C> =
    SimpleCaseStartBuilderImpl(expression)

fun case(): CaseStartBuilder =
    CaseStartBuilderImpl()

fun concat(block: ConcatContext.() -> Unit): NonNullExpression<String> =
    ConcatContext().let {
        it.block()
        it.createExpression()
    }

fun Expression<*>.count(distinct: Boolean = false): NonNullExpression<Long> =
    AggregationExpression(
        "count",
        this,
        if (distinct) "distinct" else null
    )

fun <T: Number> Expression<T>.min(): Expression<T> =
    AggregationExpression("min", this)

fun <T: Number> Expression<T>.max(): Expression<T> =
    AggregationExpression("max", this)

fun <T: Number> Expression<T>.sum(): NonNullExpression<T> =
    AggregationExpression("sum", this)

fun <T: Number> Expression<T>.avg(): NonNullExpression<T> =
    AggregationExpression("avg", this)