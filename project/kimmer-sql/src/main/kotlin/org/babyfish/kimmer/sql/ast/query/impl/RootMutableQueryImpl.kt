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
import kotlin.reflect.KClass

internal class RootMutableQueryImpl<E, ID>(
    sqlClient: SqlClientImpl,
    type: KClass<E>
): AbstractMutableQueryImpl<E, ID>(TableAliasAllocator(), sqlClient, type),
    MutableRootQuery<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

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