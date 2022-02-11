package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.ast.SqlQuery
import org.babyfish.kimmer.sql.ast.TypedSqlQuery
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

interface SqlClient {

    val entityTypeMap: Map<KClass<out Immutable>, EntityType>

    fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: SqlQuery<E, ID>.() -> TypedSqlQuery<E, ID, R>
    ): TypedSqlQuery<E, ID, R>
}
