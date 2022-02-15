package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.selectable.RootSelectable

interface ConfigurableTypedRootQuery<E: Entity<ID>, ID: Comparable<ID>, R> : TypedRootQuery<E, ID, R> {

    fun <X: Any> reselect(
        block: RootSelectable<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, X>
    ): ConfigurableTypedRootQuery<E, ID, X>

    fun distinct(distinct: Boolean = true): ConfigurableTypedRootQuery<E, ID, R>

    fun limit(limit: Int, offset: Int = 0): ConfigurableTypedRootQuery<E, ID, R>

    fun withoutSortingAndPaging(without: Boolean = true): ConfigurableTypedRootQuery<E, ID, R>
}