package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Filterable<T: Immutable> {

    val table: JoinableTable<T>

    fun where(vararg predicates: Expression<Boolean>?)

    fun orderBy(expression: Expression<*>?, descending: Boolean = false)

    fun orderBy(prop: KProperty1<T, *>, descending: Boolean = false) {
        orderBy(table[prop], descending)
    }

    fun <X: Immutable> subQuery(
        type: KClass<X>,
        block: (SqlSubQuery<T, X>.() -> Unit)? = null
    ): SqlSubQuery<T, X>

    fun <X: Immutable, R> typedSubQuery(
        type: KClass<X>,
        block: SqlSubQuery<T, X>.() -> TypedSqlSubQuery<T, X, R>
    ): TypedSqlSubQuery<T, X, R>
}