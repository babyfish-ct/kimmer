package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.ast.SqlQuery
import org.babyfish.kimmer.sql.ast.TypedSqlQuery
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

interface SqlClient {

    val entityTypeMap: Map<KClass<out Immutable>, EntityType>

    fun <T: Immutable, R> createQuery(
        type: KClass<T>,
        block: SqlQuery<T>.() -> TypedSqlQuery<T, R>
    ): TypedSqlQuery<T, R>
}
