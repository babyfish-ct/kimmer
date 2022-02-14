package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.accept

data class Tuple4<out T1, out T2, out T3, out T4>(
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4
)

data class Tuple5<out T1, out T2, out T3, out T4, out T5>(
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5
)

data class Tuple6<out T1, out T2, out T3, out T4, out T5, out T6>(
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6
)

data class Tuple7<out T1, out T2, out T3, out T4, out T5, out T6, out T7>(
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7
)

data class Tuple8<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8>(
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7,
    val _8: T8
)

data class Tuple9<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8, out T9>(
    val _1: T1,
    val _2: T2,
    val _3: T3,
    val _4: T4,
    val _5: T5,
    val _6: T6,
    val _7: T7,
    val _8: T8,
    val _9: T9
)

internal class PairExpression<A, B>(
    private val a: Selection<A>,
    private val b: Selection<B>
): AbstractExpression<Pair<A, B>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        lowestPrecedence(true) {
            render(a)
            sql(", ")
            render(b)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        a.accept(visitor)
        b.accept(visitor)
    }
}

internal class TripleExpression<A, B, C>(
    private val a: Selection<A>,
    private val b: Selection<B>,
    private val c: Selection<C>
): AbstractExpression<Triple<A, B, C>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(a)
            sql(", ")
            render(b)
            sql(", ")
            render(c)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        a.accept(visitor)
        b.accept(visitor)
        c.accept(visitor)
    }
}

internal class Tuple4Expression<T1, T2, T3, T4>(
    private val selection1: Selection<T1>,
    private val selection2: Selection<T2>,
    private val selection3: Selection<T3>,
    private val selection4: Selection<T4>,
): AbstractExpression<Tuple4<T1, T2, T3, T4>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(selection1)
            sql(", ")
            render(selection2)
            sql(", ")
            render(selection3)
            sql(", ")
            render(selection4)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selection1.accept(visitor)
        selection2.accept(visitor)
        selection3.accept(visitor)
        selection4.accept(visitor)
    }
}

internal class Tuple5Expression<T1, T2, T3, T4, T5>(
    private val selection1: Selection<T1>,
    private val selection2: Selection<T2>,
    private val selection3: Selection<T3>,
    private val selection4: Selection<T4>,
    private val selection5: Selection<T5>,
): AbstractExpression<Tuple5<T1, T2, T3, T4, T5>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(selection1)
            sql(", ")
            render(selection2)
            sql(", ")
            render(selection3)
            sql(", ")
            render(selection4)
            sql(", ")
            render(selection5)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selection1.accept(visitor)
        selection2.accept(visitor)
        selection3.accept(visitor)
        selection4.accept(visitor)
        selection5.accept(visitor)
    }
}

internal class Tuple6Expression<T1, T2, T3, T4, T5, T6>(
    private val selection1: Selection<T1>,
    private val selection2: Selection<T2>,
    private val selection3: Selection<T3>,
    private val selection4: Selection<T4>,
    private val selection5: Selection<T5>,
    private val selection6: Selection<T6>,
): AbstractExpression<Tuple6<T1, T2, T3, T4, T5, T6>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(selection1)
            sql(", ")
            render(selection2)
            sql(", ")
            render(selection3)
            sql(", ")
            render(selection4)
            sql(", ")
            render(selection5)
            sql(", ")
            render(selection6)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selection1.accept(visitor)
        selection2.accept(visitor)
        selection3.accept(visitor)
        selection4.accept(visitor)
        selection5.accept(visitor)
        selection6.accept(visitor)
    }
}

internal class Tuple7Expression<T1, T2, T3, T4, T5, T6, T7>(
    private val selection1: Selection<T1>,
    private val selection2: Selection<T2>,
    private val selection3: Selection<T3>,
    private val selection4: Selection<T4>,
    private val selection5: Selection<T5>,
    private val selection6: Selection<T6>,
    private val selection7: Selection<T7>,
): AbstractExpression<Tuple7<T1, T2, T3, T4, T5, T6, T7>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(selection1)
            sql(", ")
            render(selection2)
            sql(", ")
            render(selection3)
            sql(", ")
            render(selection4)
            sql(", ")
            render(selection5)
            sql(", ")
            render(selection6)
            sql(", ")
            render(selection7)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selection1.accept(visitor)
        selection2.accept(visitor)
        selection3.accept(visitor)
        selection4.accept(visitor)
        selection5.accept(visitor)
        selection6.accept(visitor)
        selection7.accept(visitor)
    }
}

internal class Tuple8Expression<T1, T2, T3, T4, T5, T6, T7, T8>(
    private val selection1: Selection<T1>,
    private val selection2: Selection<T2>,
    private val selection3: Selection<T3>,
    private val selection4: Selection<T4>,
    private val selection5: Selection<T5>,
    private val selection6: Selection<T6>,
    private val selection7: Selection<T7>,
    private val selection8: Selection<T8>,
): AbstractExpression<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(selection1)
            sql(", ")
            render(selection2)
            sql(", ")
            render(selection3)
            sql(", ")
            render(selection4)
            sql(", ")
            render(selection5)
            sql(", ")
            render(selection6)
            sql(", ")
            render(selection7)
            sql(", ")
            render(selection8)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selection1.accept(visitor)
        selection2.accept(visitor)
        selection3.accept(visitor)
        selection4.accept(visitor)
        selection5.accept(visitor)
        selection6.accept(visitor)
        selection7.accept(visitor)
        selection8.accept(visitor)
    }
}

internal class Tuple9Expression<T1, T2, T3, T4, T5, T6, T7, T8, T9>(
    private val selection1: Selection<T1>,
    private val selection2: Selection<T2>,
    private val selection3: Selection<T3>,
    private val selection4: Selection<T4>,
    private val selection5: Selection<T5>,
    private val selection6: Selection<T6>,
    private val selection7: Selection<T7>,
    private val selection8: Selection<T8>,
    private val selection9: Selection<T9>,
): AbstractExpression<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(selection1)
            sql(", ")
            render(selection2)
            sql(", ")
            render(selection3)
            sql(", ")
            render(selection4)
            sql(", ")
            render(selection5)
            sql(", ")
            render(selection6)
            sql(", ")
            render(selection7)
            sql(", ")
            render(selection8)
            sql(", ")
            render(selection9)
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selection1.accept(visitor)
        selection2.accept(visitor)
        selection3.accept(visitor)
        selection4.accept(visitor)
        selection5.accept(visitor)
        selection6.accept(visitor)
        selection7.accept(visitor)
        selection8.accept(visitor)
        selection9.accept(visitor)
    }
}