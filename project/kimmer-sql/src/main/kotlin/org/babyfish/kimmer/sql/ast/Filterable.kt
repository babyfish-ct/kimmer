package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.table.NonNullJoinableTable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Filterable<E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullJoinableTable<E, ID>

    fun where(vararg predicates: Expression<Boolean>?)

    fun orderBy(expression: Expression<*>?, descending: Boolean = false)

    fun orderBy(prop: KProperty1<E, *>, descending: Boolean = false) {
        orderBy(table.`get?`(prop), descending)
    }

    fun <X, XID, R: Any> subQuery(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> TypedSubQuery<E, ID, X, XID, R>
    ): TypedSubQuery<E, ID, X, XID, R>
    where X: Entity<XID>, XID: Comparable<XID>

    fun <X, XID> untypedSubQuery(
        type: KClass<X>,
        block: MutableSubQuery<E, ID, X, XID>.() -> Unit
    ): MutableSubQuery<E, ID, X, XID>
    where X: Entity<XID>, XID: Comparable<XID>
}