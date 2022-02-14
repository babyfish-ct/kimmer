package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.SelectableTypedRootQuery
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import kotlin.reflect.KClass

internal class RootQueryImpl<E, ID>(
    sqlClient: SqlClientImpl,
    type: KClass<E>
): AbstractQueryImpl<E, ID>(TableAliasAllocator(), sqlClient, type),
    MutableRootQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    override fun <R: Any> select(
        selection: Selection<R>
    ): SelectableTypedRootQuery<E, ID, R> =
        SelectableTypedRootQueryImpl.select(this, selection)

    override fun <A, B> select(
        selection1: Selection<A>,
        selection2: Selection<B>
    ): SelectableTypedRootQuery<E, ID, Pair<A, B>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2)

    override fun <A, B, C> select(
        selection1: Selection<A>,
        selection2: Selection<B>,
        selection3: Selection<C>
    ): SelectableTypedRootQuery<E, ID, Triple<A, B, C>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3)

    override fun <T1, T2, T3, T4> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
    ): SelectableTypedRootQuery<E, ID, Tuple4<T1, T2, T3, T4>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3, selection4)

    override fun <T1, T2, T3, T4, T5> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
    ): SelectableTypedRootQuery<E, ID, Tuple5<T1, T2, T3, T4, T5>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5)

    override fun <T1, T2, T3, T4, T5, T6> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
    ): SelectableTypedRootQuery<E, ID, Tuple6<T1, T2, T3, T4, T5, T6>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6)

    override fun <T1, T2, T3, T4, T5, T6, T7> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>
    ): SelectableTypedRootQuery<E, ID, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6, selection7)

    override fun <T1, T2, T3, T4, T5, T6, T7, T8> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
    ): SelectableTypedRootQuery<E, ID, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8)

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
    ): SelectableTypedRootQuery<E, ID, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
        SelectableTypedRootQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9)
}