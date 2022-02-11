package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass

fun <X: Any> sql(
    type: KClass<X>,
    sql: String,
    block: SqlExpressionContext.() -> Unit
): Expression<X> {
    TODO()
}

class SqlExpressionContext {
    fun expressions(vararg expressions: Expression<*>) {}
    fun values(vararg value: Any?) {}
}

@Suppress("UNCHECKED_CAST")
fun and(
    vararg predicates: Expression<Boolean>?
): Expression<Boolean>? =
    combine("and", predicates as Array<Expression<Boolean>?>)

@Suppress("UNCHECKED_CAST")
fun or(
    vararg predicates: Expression<Boolean>?
): Expression<Boolean>? =
    combine("or", predicates as Array<Expression<Boolean>?>)

private fun combine(
    separator: String,
    predicates: Array<Expression<Boolean>?>
): Expression<Boolean>? =
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
    predicate: Expression<Boolean>?
): Expression<Boolean>? =
    predicate?.let {
        NotExpression(it)
    }

infix fun Expression<String>.like(
    value: String
): Expression<Boolean> =
    like(value, LikeMode.ANYWHERE)

fun Expression<String>.like(
    value: String,
    likeMode: LikeMode
): Expression<Boolean> =
    LikeExpression(this, value, false, likeMode)


infix fun Expression<String>.ilike(
    value: String
): Expression<Boolean> =
    ilike(value, LikeMode.ANYWHERE)

fun Expression<String>.ilike(
    value: String,
    likeMode: LikeMode
): Expression<Boolean> =
    LikeExpression(this, value, true, likeMode)


infix fun <T: Comparable<T>> Expression<T>.eq(
    value: T
): Expression<Boolean> = eq(value(value))

infix fun <T: Comparable<T>> Expression<T>.eq(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("=", this, other)


infix fun <T: Comparable<T>> Expression<T>.ne(
    value: T
): Expression<Boolean> =
    ne(value(value))

infix fun <T: Comparable<T>> Expression<T>.ne(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("<>", this, other)


infix fun <T: Comparable<T>> Expression<T>.lt(
    value: T
): Expression<Boolean> =
    lt(value(value))

infix fun <T: Comparable<T>> Expression<T>.lt(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("<", this, other)


infix fun <T: Comparable<T>> Expression<T>.le(
    value: T
): Expression<Boolean> =
    le(value(value))

infix fun <T: Comparable<T>> Expression<T>.le(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("<=", this, other)


infix fun <T: Comparable<T>> Expression<T>.gt(
    value: T
): Expression<Boolean> =
    gt(value(value))

infix fun <T: Comparable<T>> Expression<T>.gt(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression(">", this, other)


infix fun <T: Comparable<T>> Expression<T>.ge(
    value: T
): Expression<Boolean> =
    ge(value(value))

infix fun <T: Comparable<T>> Expression<T>.ge(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression(">=", this, other)


fun <T: Comparable<T>> Expression<T>.between(
    min: T,
    max: T
): Expression<Boolean> =
    between(value(min), value(max))

fun <T: Comparable<T>> Expression<T>.between(
    min: Expression<T>,
    max: Expression<T>
): Expression<Boolean> =
    BetweenExpression(this, min, max)


operator fun <T: Number> Expression<T>.plus(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.plus(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("+", this, other)


operator fun <T: Number> Expression<T>.minus(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.minus(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("-", this, other)


operator fun <T: Number> Expression<T>.times(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.times(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("*", this, other)


operator fun <T: Number> Expression<T>.div(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.div(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("/", this, other)


operator fun <T: Number> Expression<T>.rem(
    value: T
): Expression<T> =
    plus(value(value))

operator fun <T: Number> Expression<T>.rem(
    other: Expression<T>
): Expression<T> =
    BinaryExpression("%", this, other)


operator fun <T: Number> Expression<T>.unaryPlus(): Expression<T> =
    UnaryExpression("+", this)


operator fun <T: Number> Expression<T>.unaryMinus(): Expression<T> =
    UnaryExpression("-", this)


fun Expression<*>.isNull(): Expression<Boolean> =
    NullityExpression(true, this)

fun Expression<*>.isNotNull(): Expression<Boolean> =
    NullityExpression(false, this)

fun <A, B> tuple(
    a: Selection<A>,
    b: Selection<B>
): Expression<Pair<A, B>> =
    PairExpression(a, b)

fun <A, B, C> tuple(
    a: Selection<A>,
    b: Selection<B>,
    c: Selection<C>
): Expression<Triple<A, B, C>> =
    TripleExpression(a, b, c)

fun <T1, T2, T3, T4> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
): Expression<Tuple4<T1, T2, T3, T4>> =
    Tuple4Expression(selection1, selection2, selection3, selection4)

fun <T1, T2, T3, T4, T5> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
): Expression<Tuple5<T1, T2, T3, T4, T5>> =
    Tuple5Expression(selection1, selection2, selection3, selection4, selection5)

fun <T1, T2, T3, T4, T5, T6> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
): Expression<Tuple6<T1, T2, T3, T4, T5, T6>> =
    Tuple6Expression(selection1, selection2, selection3, selection4, selection5, selection6)

fun <T1, T2, T3, T4, T5, T6, T7> tuple(
    selection1: Selection<T1>,
    selection2: Selection<T2>,
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
    selection7: Selection<T7>,
): Expression<Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
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
): Expression<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
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
): Expression<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
    Tuple9Expression(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9)

infix fun <T> Expression<T>.valueIn(
    values: Collection<T>
): Expression<Boolean> =
    InListExpression(false, this, values)

infix fun <T> Expression<T>.valueNotIn(
    values: Collection<T>
): Expression<Boolean> =
    InListExpression(true, this, values)


infix fun <T> Expression<T>.valueIn(
    subQuery: TypedSqlSubQuery<*, *, *, *, T>
): Expression<Boolean> =
    InSubQueryExpression(false, this, subQuery)

infix fun <T> Expression<T>.valueNotIn(
    subQuery: TypedSqlSubQuery<*, *, *, *, T>
): Expression<Boolean> =
    InSubQueryExpression(true, this, subQuery)


fun exists(
    subQuery: SqlSubQuery<*, *, *, *>
): Expression<Boolean> =
    ExistsExpression(false, subQuery)

fun notExists(
    subQuery: SqlSubQuery<*, *, *, *>
): Expression<Boolean> =
    ExistsExpression(true, subQuery)


fun <T> all(
    subQuery: TypedSqlSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("all", subQuery)

fun <T> any(
    subQuery: TypedSqlSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("any", subQuery)

fun <T> some(
    subQuery: TypedSqlSubQuery<*, *, *, *, T>
): Expression<T> =
    OperatorSubQueryExpression("some", subQuery)

fun <T> value(value: T): Expression<T> =
    ValueExpression(value)

fun <T: Number> constant(value: T): Expression<T> =
    ConstantExpression(value)

fun <C> case(expression: Expression<C>): SimpleCaseStartBuilder<C> =
    SimpleCaseStartBuilderImpl(expression)

fun case(): CaseStartBuilder =
    CaseStartBuilderImpl()

fun concat(block: ConcatContext.() -> Unit): Expression<String> =
    ConcatContext().let {
        it.block()
        it.createExpression()
    }

fun Expression<*>.count(distinct: Boolean = false): Expression<Long> =
    AggregationExpression(
        "count",
        this,
        if (distinct) "distinct" else null
    )

fun <T: Number> Expression<T>.min(): Expression<T> =
    AggregationExpression("min", this)

fun <T: Number> Expression<T>.max(): Expression<T> =
    AggregationExpression("max", this)

fun <T: Number> Expression<T>.sum(): Expression<T> =
    AggregationExpression("sum", this)

fun <T: Number> Expression<T>.avg(): Expression<T> =
    AggregationExpression("avg", this)