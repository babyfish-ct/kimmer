package org.babyfish.kimmer.sql.ast.query.selectable

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.NonNullTable

object ProjectionContext {

    infix fun <T: Any, X: Any> NonNullExpression<T>.then(
        other: NonNullExpression<X>
    ): Projection2<T, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Any> NonNullExpression<T>.then(
        other: Expression<X>
    ): Projection2<T, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Entity<*>> NonNullExpression<T>.then(
        other: NonNullTable<X, *>
    ): Projection2<T, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Entity<*>> NonNullExpression<T>.then(
        other: Table<X, *>
    ): Projection2<T, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Any> Expression<T>.then(
        other: NonNullExpression<X>
    ): Projection2<T?, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Any> Expression<T>.then(
        other: Expression<X>
    ): Projection2<T?, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Entity<*>> Expression<T>.then(
        other: NonNullTable<X, *>
    ): Projection2<T?, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Any, X: Entity<*>> Expression<T>.then(
        other: Table<X, *>
    ): Projection2<T?, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Any> NonNullTable<T, ID>.then(
        other: NonNullExpression<X>
    ): Projection2<T, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Any> NonNullTable<T, ID>.then(
        other: Expression<X>
    ): Projection2<T, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Entity<*>> NonNullTable<T, ID>.then(
        other: NonNullTable<X, *>
    ): Projection2<T, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Entity<*>> NonNullTable<T, ID>.then(
        other: Table<X, *>
    ): Projection2<T, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Any> Table<T, ID>.then(
        other: NonNullExpression<X>
    ): Projection2<T?, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Any> Table<T, ID>.then(
        other: Expression<X>
    ): Projection2<T?, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Entity<*>> Table<T, ID>.then(
        other: NonNullTable<X, *>
    ): Projection2<T?, X> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)

    infix fun <T: Entity<ID>, ID: Comparable<ID>, X: Entity<*>> Table<T, ID>.then(
        other: Table<X, *>
    ): Projection2<T?, X?> =
        Projection2Impl(this as Selection<*>, other as Selection<*>)
}

interface Projection<T: Any>

interface Projection2<T1, T2>: Projection<Pair<T1, T2>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection3<T1, T2, X>

    infix fun <X: Any> then(other: Expression<X>): Projection3<T1, T2, X?>
    
    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection3<T1, T2, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection3<T1, T2, X?>
}

interface Projection3<T1, T2, T3>: Projection<Triple<T1, T2, T3>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection4<T1, T2, T3, X>

    infix fun <X: Any> then(other: Expression<X>): Projection4<T1, T2, T3, X?>

    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection4<T1, T2, T3, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection4<T1, T2, T3, X?>
}

interface Projection4<T1, T2, T3, T4>: Projection<Tuple4<T1, T2, T3, T4>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection5<T1, T2, T3, T4, X>

    infix fun <X: Any> then(other: Expression<X>): Projection5<T1, T2, T3, T4, X?>

    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection5<T1, T2, T3, T4, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection5<T1, T2, T3, T4, X?>
}

interface Projection5<T1, T2, T3, T4, T5>: Projection<Tuple5<T1, T2, T3, T4, T5>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection6<T1, T2, T3, T4, T5, X>

    infix fun <X: Any> then(other: Expression<X>): Projection6<T1, T2, T3, T4, T5, X?>

    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection6<T1, T2, T3, T4, T5, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection6<T1, T2, T3, T4, T5, X?>
}

interface Projection6<T1, T2, T3, T4, T5, T6>: Projection<Tuple6<T1, T2, T3, T4, T5, T6>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection7<T1, T2, T3, T4, T5, T6, X>

    infix fun <X: Any> then(other: Expression<X>): Projection7<T1, T2, T3, T4, T5, T6, X?>

    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection7<T1, T2, T3, T4, T5, T6, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection7<T1, T2, T3, T4, T5, T6, X?>
}

interface Projection7<T1, T2, T3, T4, T5, T6, T7>: Projection<Tuple7<T1, T2, T3, T4, T5, T6, T7>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection8<T1, T2, T3, T4, T5, T6, T7, X>

    infix fun <X: Any> then(other: Expression<X>): Projection8<T1, T2, T3, T4, T5, T6, T7, X?>

    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection8<T1, T2, T3, T4, T5, T6, T7, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection8<T1, T2, T3, T4, T5, T6, T7, X?>
}

interface Projection8<T1, T2, T3, T4, T5, T6, T7, T8>: Projection<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> {

    infix fun <X: Any> then(other: NonNullExpression<X>): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X>

    infix fun <X: Any> then(other: Expression<X>): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X?>

    infix fun <X: Entity<*>> then(other: NonNullTable<X, *>): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X>

    infix fun <X: Entity<*>> then(other: Table<X, *>): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X?>
}

interface Projection9<T1, T2, T3, T4, T5, T6, T7, T8, T9>: Projection<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>


internal abstract class AbstractProjection(
    private val parent: Any,
    private val selection: Selection<*>
) {
    val selections: List<Selection<*>> by lazy {
        mutableListOf<Selection<*>>().also {
            collect(it)
        }
    }

    private fun collect(selections: MutableList<Selection<*>>) {
        when (parent) {
            is Selection<*> -> selections += parent
            is AbstractProjection -> parent.collect(selections)
            else -> error("Internal bug: invalid project parent")
        }
        selections += selection
    }
}

internal class Projection2Impl<T1, T2>(
    parent: Selection<*>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection2<T1, T2> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection3<T1, T2, X> =
        Projection3Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection3<T1, T2, X?> =
        Projection3Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection3<T1, T2, X> =
        Projection3Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection3<T1, T2, X?> =
        Projection3Impl(this, other as Selection<*>)
}

internal class Projection3Impl<T1, T2, T3>(
    parent: Projection2<T1, T2>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection3<T1, T2, T3> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection4<T1, T2, T3, X> =
        Projection4Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection4<T1, T2, T3, X?> =
        Projection4Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection4<T1, T2, T3, X> =
        Projection4Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection4<T1, T2, T3, X?> =
        Projection4Impl(this, other as Selection<*>)
}

internal class Projection4Impl<T1, T2, T3, T4>(
    parent: Projection3<T1, T2, T3>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection4<T1, T2, T3, T4> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection5<T1, T2, T3, T4, X> =
        Projection5Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection5<T1, T2, T3, T4, X?> =
        Projection5Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection5<T1, T2, T3, T4, X> =
        Projection5Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection5<T1, T2, T3, T4, X?> =
        Projection5Impl(this, other as Selection<*>)
}

internal class Projection5Impl<T1, T2, T3, T4, T5>(
    parent: Projection4<T1, T2, T3, T4>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection5<T1, T2, T3, T4, T5> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection6<T1, T2, T3, T4, T5, X> =
        Projection6Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection6<T1, T2, T3, T4, T5, X?> =
        Projection6Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection6<T1, T2, T3, T4, T5, X> =
        Projection6Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection6<T1, T2, T3, T4, T5, X?> =
        Projection6Impl(this, other as Selection<*>)
}

internal class Projection6Impl<T1, T2, T3, T4, T5, T6>(
    parent: Projection5<T1, T2, T3, T4, T5>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection6<T1, T2, T3, T4, T5, T6> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection7<T1, T2, T3, T4, T5, T6, X> =
        Projection7Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection7<T1, T2, T3, T4, T5, T6, X?> =
        Projection7Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection7<T1, T2, T3, T4, T5, T6, X> =
        Projection7Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection7<T1, T2, T3, T4, T5, T6, X?> =
        Projection7Impl(this, other as Selection<*>)
}

internal class Projection7Impl<T1, T2, T3, T4, T5, T6, T7>(
    parent: Projection6<T1, T2, T3, T4, T5, T6>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection7<T1, T2, T3, T4, T5, T6, T7> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection8<T1, T2, T3, T4, T5, T6, T7, X> =
        Projection8Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection8<T1, T2, T3, T4, T5, T6, T7, X?> =
        Projection8Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection8<T1, T2, T3, T4, T5, T6, T7, X> =
        Projection8Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection8<T1, T2, T3, T4, T5, T6, T7, X?> =
        Projection8Impl(this, other as Selection<*>)
}

internal class Projection8Impl<T1, T2, T3, T4, T5, T6, T7, T8>(
    parent: Projection7<T1, T2, T3, T4, T5, T6, T7>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection8<T1, T2, T3, T4, T5, T6, T7, T8> {

    override fun <X : Any> then(
        other: NonNullExpression<X>
    ): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X> =
        Projection9Impl(this, other as Selection<*>)

    override fun <X : Any> then(
        other: Expression<X>
    ): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X?> =
        Projection9Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: NonNullTable<X, *>
    ): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X> =
        Projection9Impl(this, other as Selection<*>)

    override fun <X : Entity<*>> then(
        other: Table<X, *>
    ): Projection9<T1, T2, T3, T4, T5, T6, T7, T8, X?> =
        Projection9Impl(this, other as Selection<*>)
}

internal class Projection9Impl<T1, T2, T3, T4, T5, T6, T7, T8, T9>(
    parent: Projection8<T1, T2, T3, T4, T5, T6, T7, T8>,
    selection: Selection<*>
) : AbstractProjection(parent, selection), Projection9<T1, T2, T3, T4, T5, T6, T7, T8, T9> {

}