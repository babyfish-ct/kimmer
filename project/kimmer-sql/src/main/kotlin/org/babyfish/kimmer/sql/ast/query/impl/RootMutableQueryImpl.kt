package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.selectable.AbstractProjection
import org.babyfish.kimmer.sql.ast.query.selectable.Projection
import org.babyfish.kimmer.sql.ast.query.selectable.ProjectionContext
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.NonNullTable
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class RootMutableQueryImpl<E, ID>(
    sqlClient: SqlClientImpl,
    entityType: EntityType
): AbstractMutableQueryImpl<E, ID>(
    TableAliasAllocator(),
    sqlClient,
    entityType
),
    MutableRootQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    constructor(
        sqlClient: SqlClientImpl,
        type: KClass<E>
    ): this(sqlClient, sqlClient.entityTypeOf(type))

    constructor(
        sqlClient: SqlClientImpl,
        prop: KProperty1<*, *>
    ): this(sqlClient, sqlClient.associationEntityTypeOf(prop))

    override fun <X : Any> select(
        expression: NonNullExpression<X>
    ): ConfigurableTypedRootQuery<E, ID, X> =
        ConfigurableTypedRootQueryImpl.select(this, listOf(expression as Selection<*>))

    override fun <X : Any> select(
        expression: Expression<X>
    ): ConfigurableTypedRootQuery<E, ID, X?> =
        ConfigurableTypedRootQueryImpl.select(this, listOf(expression as Selection<*>))

    override fun <X : Entity<XID>, XID : Comparable<XID>> select(
        table: NonNullTable<X, XID>
    ): ConfigurableTypedRootQuery<E, ID, X> =
        ConfigurableTypedRootQueryImpl.select(this, listOf(table as Selection<*>))

    override fun <X : Entity<XID>, XID : Comparable<XID>> select(
        table: Table<X, XID>
    ): ConfigurableTypedRootQuery<E, ID, X?> =
        ConfigurableTypedRootQueryImpl.select(this, listOf(table as Selection<*>))

    override fun <X: Any> select(
        block: ProjectionContext.() -> Projection<X>
    ): ConfigurableTypedRootQuery<E, ID, X> =
        ConfigurableTypedRootQueryImpl.select(this, (ProjectionContext.block() as AbstractProjection).selections)
}