package org.babyfish.kimmer.sql.ast

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

internal class TupleExpression<T: Any>(
    private val selections: List<Selection<*>>
): AbstractExpression<T>(null) {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        lowestPrecedence(true) {
            var sp: String? = null
            for (selection in selections) {
                if (sp === null) {
                    sp = ", "
                } else {
                    sql(sp)
                }
                render(selection)
            }
        }
    }

    override fun accept(visitor: AstVisitor) {
        selections.forEach {
            it.accept(visitor)
        }
    }
}
