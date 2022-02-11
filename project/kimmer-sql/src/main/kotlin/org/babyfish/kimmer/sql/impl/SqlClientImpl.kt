package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.QueryImpl
import org.babyfish.kimmer.sql.ast.SqlQuery
import org.babyfish.kimmer.sql.ast.TypedSqlQuery
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

internal class SqlClientImpl(
    override val entityTypeMap: Map<KClass<out Immutable>, EntityType>,
    internal val jdbcExecutor: ((String, List<Any?>) -> List<*>)?,
    internal val r2dbcExecutor: (suspend (String, List<Any?>) -> List<*>)?
) : SqlClient {

    override fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: SqlQuery<E, ID>.() -> TypedSqlQuery<E, ID, R>
    ): TypedSqlQuery<E, ID, R> =
        QueryImpl(this, type).run {
            block()
        }
}