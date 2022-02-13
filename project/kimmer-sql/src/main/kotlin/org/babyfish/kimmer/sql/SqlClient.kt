package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.SelectableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

interface SqlClient {

    val entityTypeMap: Map<KClass<out Immutable>, EntityType>

    fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> SelectableTypedRootQuery<E, ID, R>
    ): SelectableTypedRootQuery<E, ID, R>
}
