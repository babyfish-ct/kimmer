package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class SubQueryImpl<P: Immutable, T: Immutable>(
    private val parentQuery: AbstractQueryImpl<P>,
    type: KClass<T>,
): AbstractQueryImpl<T>(
    parentQuery.tableAliasAllocator,
    parentQuery.sqlClient,
    type
), SqlSubQuery<P, T> {

    override val parentTable: JoinableTable<P>
        get() = parentQuery.table

    override fun <R> select(
        prop: KProperty1<T, R?>
    ): TypedSqlSubQuery<P, T, R> =
        TypedSubQueryImpl(listOf(table[prop]), this)

    override fun <R> select(
        selection: Selection<R>
    ): TypedSqlSubQuery<P, T, R> =
        TypedSubQueryImpl(listOf(selection), this)

    override fun <A, B> select(
        selection1: Selection<A>,
        selection2: Selection<B>
    ): TypedSqlSubQuery<P, T, Pair<A, B>> =
        TypedSubQueryImpl(listOf(selection1, selection2), this)

    override fun <A, B, C> select(
        selection1: Selection<A>,
        selection2: Selection<B>,
        selection3: Selection<C>
    ): TypedSqlSubQuery<P, T, Triple<A, B, C>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3), this)

    override fun <T1, T2, T3, T4> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
    ): TypedSqlSubQuery<P, T, Tuple4<T1, T2, T3, T4>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3, selection4), this)

    override fun <T1, T2, T3, T4, T5> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
    ): TypedSqlSubQuery<P, T, Tuple5<T1, T2, T3, T4, T5>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5), this)

    override fun <T1, T2, T3, T4, T5, T6> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
    ): TypedSqlSubQuery<P, T, Tuple6<T1, T2, T3, T4, T5, T6>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6), this)

    override fun <T1, T2, T3, T4, T5, T6, T7> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>
    ): TypedSqlSubQuery<P, T, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7), this)

    override fun <T1, T2, T3, T4, T5, T6, T7, T8> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
    ): TypedSqlSubQuery<P, T, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8), this)

    override fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
        selection9: Selection<T9>,
    ): TypedSqlSubQuery<P, T, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
        TypedSubQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9), this)
}