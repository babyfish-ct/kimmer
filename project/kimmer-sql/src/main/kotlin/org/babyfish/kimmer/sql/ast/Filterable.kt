package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.SqlSubQuery
import org.babyfish.kimmer.sql.ast.table.JoinableTable
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

    fun <X, XID, R> subQuery(
        type: KClass<X>,
        block: SqlSubQuery<E, ID, X, XID>.() -> TypedSqlSubQuery<E, ID, X, XID, R>
    ): TypedSqlSubQuery<E, ID, X, XID, R>
    where X: Entity<XID>, XID: Comparable<XID>

    fun <X, XID> untypedSubQuery(
        type: KClass<X>,
        block: SqlSubQuery<E, ID, X, XID>.() -> Unit
    ): SqlSubQuery<E, ID, X, XID>
    where X: Entity<XID>, XID: Comparable<XID>
}