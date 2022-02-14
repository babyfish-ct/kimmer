package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.query.impl.RootQueryImpl
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.SelectableTypedRootQuery
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import kotlin.reflect.KClass

internal class SqlClientImpl(
    override val entityTypeMap: Map<KClass<out Immutable>, EntityType>,
    internal val jdbcExecutor: JdbcExecutor,
    internal val r2dbcExecutor: R2dbcExecutor
) : SqlClient {

    override fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> SelectableTypedRootQuery<E, ID, R>
    ): SelectableTypedRootQuery<E, ID, R> =
        RootQueryImpl(this, type).run {
            block()
        }
}