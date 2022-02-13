package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.selectable.RootSelectable

interface SelectableTypedRootQuery<E: Entity<ID>, ID: Comparable<ID>, R> : TypedRootQuery<E, ID, R> {

    fun <X> reselect(
        block: RootSelectable<E, ID>.() -> SelectableTypedRootQuery<E, ID, X>
    ): SelectableTypedRootQuery<E, ID, X>

    fun limit(limit: Int, offset: Int = 0): SelectableTypedRootQuery<E, ID, R>

    fun withoutSortingAndPaging(without: Boolean = true): SelectableTypedRootQuery<E, ID, R>
}
