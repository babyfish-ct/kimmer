package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class QueryImpl<T: Immutable>(
    val sql: SqlClientImpl,
    type: KClass<T>
): AbstractQueryImpl<T>(TableAliasAllocator(), sql.entityTypeMap, type), SqlQuery<T> {

    override fun <R> select(
        prop: KProperty1<T, R?>
    ): TypedSqlQuery<T, R> =
        TypedQueryImpl(listOf(table[prop]), this)

    override fun <R> select(
        selection: Selection<R>
    ): TypedSqlQuery<T, R> =
        TypedQueryImpl(listOf(selection), this)

    override fun <A, B> select(
        selection1: Selection<A>,
        selection2: Selection<B>
    ): TypedSqlQuery<T, Pair<A, B>> =
        TypedQueryImpl(listOf(selection1, selection2), this)

    override fun <A, B, C> select(
        selection1: Selection<A>,
        selection2: Selection<B>,
        selection3: Selection<C>
    ): TypedSqlQuery<T, Triple<A, B, C>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3), this)

    override fun <T1, T2, T3, T4> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
    ): TypedSqlQuery<T, Tuple4<T1, T2, T3, T4>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4), this)

    override fun <T1, T2, T3, T4, T5> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
    ): TypedSqlQuery<T, Tuple5<T1, T2, T3, T4, T5>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5), this)

    override fun <T1, T2, T3, T4, T5, T6> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
    ): TypedSqlQuery<T, Tuple6<T1, T2, T3, T4, T5, T6>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6), this)

    override fun <T1, T2, T3, T4, T5, T6, T7> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>
    ): TypedSqlQuery<T, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7), this)

    override fun <T1, T2, T3, T4, T5, T6, T7, T8> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
    ): TypedSqlQuery<T, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8), this)

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
    ): TypedSqlQuery<T, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9), this)
}