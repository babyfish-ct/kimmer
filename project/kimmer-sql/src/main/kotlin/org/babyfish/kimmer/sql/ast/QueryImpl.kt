package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class QueryImpl<E, ID>(
    val sql: SqlClientImpl,
    type: KClass<E>
): AbstractQueryImpl<E, ID>(TableAliasAllocator(), sql, type), 
    SqlQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    override fun <R> select(
        prop: KProperty1<E, R?>
    ): TypedSqlQuery<E, ID, R> =
        TypedQueryImpl(listOf(table[prop]), this)

    override fun <R> select(
        selection: Selection<R>
    ): TypedSqlQuery<E, ID, R> =
        TypedQueryImpl(listOf(selection), this)

    override fun <A, B> select(
        selection1: Selection<A>,
        selection2: Selection<B>
    ): TypedSqlQuery<E, ID, Pair<A, B>> =
        TypedQueryImpl(listOf(selection1, selection2), this)

    override fun <A, B, C> select(
        selection1: Selection<A>,
        selection2: Selection<B>,
        selection3: Selection<C>
    ): TypedSqlQuery<E, ID, Triple<A, B, C>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3), this)

    override fun <T1, T2, T3, T4> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
    ): TypedSqlQuery<E, ID, Tuple4<T1, T2, T3, T4>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4), this)

    override fun <T1, T2, T3, T4, T5> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
    ): TypedSqlQuery<E, ID, Tuple5<T1, T2, T3, T4, T5>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5), this)

    override fun <T1, T2, T3, T4, T5, T6> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
    ): TypedSqlQuery<E, ID, Tuple6<T1, T2, T3, T4, T5, T6>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6), this)

    override fun <T1, T2, T3, T4, T5, T6, T7> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>
    ): TypedSqlQuery<E, ID, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
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
    ): TypedSqlQuery<E, ID, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
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
    ): TypedSqlQuery<E, ID, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
        TypedQueryImpl(listOf(selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9), this)
}