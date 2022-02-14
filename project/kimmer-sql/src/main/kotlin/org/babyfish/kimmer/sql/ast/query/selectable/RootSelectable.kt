package org.babyfish.kimmer.sql.ast.query.selectable

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.SelectableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.babyfish.kimmer.sql.ast.query.impl.SelectableTypedRootQueryImpl
import org.babyfish.kimmer.sql.ast.table.NonNullJoinableTable

interface RootSelectable<E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullJoinableTable<E, ID>

    fun <R: Any> select(
        selection: Selection<R>
    ): SelectableTypedRootQuery<E, ID, R>

    fun <A, B> select(
        selection1: Selection<A>,
        selection2: Selection<B>
    ): SelectableTypedRootQuery<E, ID, Pair<A, B>>

    fun <A, B, C> select(
        selection1: Selection<A>,
        selection2: Selection<B>,
        selection3: Selection<C>
    ): SelectableTypedRootQuery<E, ID, Triple<A, B, C>>

    fun <T1, T2, T3, T4> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
    ): SelectableTypedRootQuery<E, ID, Tuple4<T1, T2, T3, T4>>

    fun <T1, T2, T3, T4, T5> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
    ): SelectableTypedRootQuery<E, ID, Tuple5<T1, T2, T3, T4, T5>>

    fun <T1, T2, T3, T4, T5, T6> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
    ): SelectableTypedRootQuery<E, ID, Tuple6<T1, T2, T3, T4, T5, T6>>

    fun <T1, T2, T3, T4, T5, T6, T7> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>
    ): SelectableTypedRootQuery<E, ID, Tuple7<T1, T2, T3, T4, T5, T6, T7>>

    fun <T1, T2, T3, T4, T5, T6, T7, T8> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
    ): SelectableTypedRootQuery<E, ID, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>>

    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
        selection9: Selection<T9>,
    ): SelectableTypedRootQuery<E, ID, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
}