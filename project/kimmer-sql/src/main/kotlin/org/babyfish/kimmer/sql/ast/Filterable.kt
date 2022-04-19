package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.SubQueries
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.query.WildSubQueries
import org.babyfish.kimmer.sql.ast.table.NonNullTable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Filterable<E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullTable<E, ID>

    fun where(vararg predicates: NonNullExpression<Boolean>?)

    fun where(block: () -> NonNullExpression<Boolean>?)

    fun orderBy(
        expression: Expression<*>?,
        mode: OrderMode = OrderMode.ASC,
        nullMode: NullOrderMode = NullOrderMode.UNSPECIFIED
    )

    fun orderBy(
        prop: KProperty1<E, *>,
        mode: OrderMode = OrderMode.ASC,
        nullMode: NullOrderMode = NullOrderMode.UNSPECIFIED
    ) {
        orderBy(table.`get?`(prop), mode, nullMode)
    }

    fun <X, XID, R: Any> subQuery(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> TypedSubQuery<R>
    ): TypedSubQuery<R>
    where X: Entity<XID>, XID: Comparable<XID> =
        subQueries.byType(type, block)

    fun <X, XID> wildSubQuery(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> Unit
    ): MutableSubQuery<E, ID, X, XID>
    where X: Entity<XID>, XID: Comparable<XID> =
        wildSubQueries.byType(type, block)

    val subQueries: SubQueries<E, ID>

    val wildSubQueries: WildSubQueries<E, ID>
}

enum class OrderMode {
    ASC,
    DESC
}

enum class NullOrderMode {
    UNSPECIFIED,
    NULLS_FIRST,
    NULLS_LAST
}