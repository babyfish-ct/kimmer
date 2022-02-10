package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import kotlin.reflect.KProperty1

interface JoinableTable<T: Immutable>: Table<T> {

    fun <X: Immutable> joinReference(
        prop: KProperty1<out T, X?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X>

    fun <X: Immutable> joinList(
        prop: KProperty1<out T, List<X>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X>

    fun <X: Immutable> joinConnection(
        prop: KProperty1<out T, Connection<X>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X>
}