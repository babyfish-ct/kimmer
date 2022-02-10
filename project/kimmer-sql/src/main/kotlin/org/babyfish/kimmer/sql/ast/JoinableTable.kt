package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

interface JoinableTable<T: Immutable> : Table<T>, Selection<T> {

    fun <X: Immutable> joinReference(
        prop: KProperty1<T, X?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X>

    fun <X: Immutable> joinList(
        prop: KProperty1<T, List<X>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X>

    fun <X: Immutable> joinConnection(
        prop: KProperty1<T, Connection<X>?>,
        joinType: JoinType = JoinType.INNER
    ): JoinableTable<X>
}