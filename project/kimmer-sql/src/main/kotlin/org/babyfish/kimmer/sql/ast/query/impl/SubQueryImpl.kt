package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.SelectableTypedSubQuery
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.impl.SubQueryTableImpl
import org.babyfish.kimmer.sql.ast.table.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class SubQueryImpl<P, PID, E, ID>(
    private val parentQuery: AbstractQueryImpl<P, PID>,
    type: KClass<E>,
): AbstractQueryImpl<E, ID>(
        parentQuery.tableAliasAllocator,
        parentQuery.sqlClient,
        type
    ),
    MutableSubQuery<P, PID, E, ID>,
    Renderable,
    TableReferenceElement
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    override val table: SubQueryTableImpl<E, ID>
        get() = super.table as SubQueryTableImpl<E, ID>

    override fun createTable(entityType: EntityType): TableImpl<E, ID> =
        SubQueryTableImpl(this, entityType)

    override val parentTable: Table<P, PID>
        get() = parentQuery.table

    override fun accept(visitor: TableReferenceVisitor) {
        accept(visitor, false)
    }

    override fun renderTo(builder: SqlBuilder) {
         renderTo(builder, false)
    }

    override fun <R: Any> select(
        selection: Selection<R>
    ): SelectableTypedSubQuery<P, PID, E, ID, R> =
        SelectableTypedSubQueryImpl.select(this, selection)

    override fun <A, B> select(
        selection1: Selection<A>,
        selection2: Selection<B>
    ): SelectableTypedSubQuery<P, PID, E, ID, Pair<A, B>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2)

    override fun <A, B, C> select(
        selection1: Selection<A>,
        selection2: Selection<B>,
        selection3: Selection<C>
    ): SelectableTypedSubQuery<P, PID, E, ID, Triple<A, B, C>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3)

    override fun <T1, T2, T3, T4> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
    ): SelectableTypedSubQuery<P, PID, E, ID, Tuple4<T1, T2, T3, T4>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3, selection4)

    override fun <T1, T2, T3, T4, T5> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
    ): SelectableTypedSubQuery<P, PID, E, ID, Tuple5<T1, T2, T3, T4, T5>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5)

    override fun <T1, T2, T3, T4, T5, T6> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
    ): SelectableTypedSubQuery<P, PID, E, ID, Tuple6<T1, T2, T3, T4, T5, T6>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6)

    override fun <T1, T2, T3, T4, T5, T6, T7> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>
    ): SelectableTypedSubQuery<P, PID, E, ID, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6, selection7)

    override fun <T1, T2, T3, T4, T5, T6, T7, T8> select(
        selection1: Selection<T1>,
        selection2: Selection<T2>,
        selection3: Selection<T3>,
        selection4: Selection<T4>,
        selection5: Selection<T5>,
        selection6: Selection<T6>,
        selection7: Selection<T7>,
        selection8: Selection<T8>,
    ): SelectableTypedSubQuery<P, PID, E, ID, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8)

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
    ): SelectableTypedSubQuery<P, PID, E, ID, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
        SelectableTypedSubQueryImpl.select(this, selection1, selection2, selection3, selection4, selection5, selection6, selection7, selection8, selection9)
}