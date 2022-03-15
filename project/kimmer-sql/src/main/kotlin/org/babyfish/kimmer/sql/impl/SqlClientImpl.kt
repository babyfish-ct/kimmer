package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.Entities
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.ast.MutableDelete
import org.babyfish.kimmer.sql.ast.MutableUpdate
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
    override val jdbcExecutor: JdbcExecutor,
    override val r2dbcExecutor: R2dbcExecutor
) : SqlClient {

    override fun <E: Entity<ID>, ID: Comparable<ID>, R> createQuery(
        type: KClass<E>,
        block: MutableRootQuery<E, ID>.() -> ConfigurableTypedRootQuery<E, ID, R>
    ): ConfigurableTypedRootQuery<E, ID, R> =
        RootMutableQueryImpl(this, type).run {
            block()
        }

    override fun <E: Entity<ID>, ID: Comparable<ID>> createUpdate(
        type: KClass<E>,
        block: MutableUpdate<E, ID>.() -> Unit
    ): Executable<Int> =
        MutableUpdateImpl(this, type).apply {
            block()
            freeze()
        }

    override fun <E: Entity<ID>, ID: Comparable<ID>> createDelete(
        type: KClass<E>,
        block: MutableDelete<E, ID>.() -> Unit
    ): Executable<Int> =
        MutableDeleteImpl(this, type).apply {
            block()
            freeze()
        }

    override val entities: Entities =
        EntitiesImpl(this)

//    override val trigger: Trigger
//        get() = TODO("Not yet implemented")
}