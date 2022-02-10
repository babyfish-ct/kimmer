package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

interface AbstractSqlQuery<T: Immutable>: Filterable<T> {

    fun groupBy(vararg expression: Expression<*>)

    fun groupBy(vararg props: KProperty1<T, *>) {
        groupBy(*props.map { table[it] }.toTypedArray())
    }

    fun having(vararg predicates: Expression<Boolean>?)

    fun clearWhereClauses()

    fun clearGroupByClauses()

    fun clearHavingClauses()

    fun clearOrderByClauses()
}