package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KProperty1

interface AbstractSqlQuery<E, ID>: Filterable<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    fun groupBy(vararg expression: Expression<*>)

    fun groupBy(vararg props: KProperty1<E, *>) {
        groupBy(*props.map { table[it] }.toTypedArray())
    }

    fun having(vararg predicates: Expression<Boolean>?)

    fun clearWhereClauses()

    fun clearGroupByClauses()

    fun clearHavingClauses()

    fun clearOrderByClauses()
}