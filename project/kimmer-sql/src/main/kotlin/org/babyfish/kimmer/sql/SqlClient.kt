package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.runtime.Dialect
import kotlin.reflect.KClass

interface SqlClient {

    val entityTypeMap: Map<KClass<out Immutable>, EntityType>

    val dialect: Dialect

    fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R>
}
