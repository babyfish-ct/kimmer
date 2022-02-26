package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.query.impl.RootMutableQueryImpl
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import kotlin.reflect.KClass

internal class SqlClientImpl(
    override val entityTypeMap: Map<KClass<out Entity<*>>, EntityType>,
    override val scalarProviderMap: Map<KClass<*>, ScalarProvider<*, *>>,
    override val dialect: Dialect,
    internal val jdbcExecutor: JdbcExecutor,
    internal val r2dbcExecutor: R2dbcExecutor
) : SqlClient {

    override fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R> =
        RootMutableQueryImpl(this, type).run {
            block()
        }
}