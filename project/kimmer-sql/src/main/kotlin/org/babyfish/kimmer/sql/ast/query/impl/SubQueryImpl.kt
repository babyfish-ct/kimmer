package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedSubQuery
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.query.selectable.AbstractProjection
import org.babyfish.kimmer.sql.ast.query.selectable.Projection
import org.babyfish.kimmer.sql.ast.query.selectable.ProjectionContext
import org.babyfish.kimmer.sql.ast.table.JoinableTable
import org.babyfish.kimmer.sql.ast.table.NonNullJoinableTable
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.impl.SubQueryTableImpl
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KClass

internal class SubQueryImpl<P, PID, E, ID>(
    private val parentQuery: AbstractQueryImpl<P, PID>,
    type: KClass<E>,
): AbstractQueryImpl<E, ID>(
        parentQuery.tableAliasAllocator,
        parentQuery.sqlClient,
        type
    ),
    MutableSubQuery<P, PID, E, ID>,
    Renderable,
    TableReferenceElement
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    override val table: SubQueryTableImpl<E, ID>
        get() = super.table as SubQueryTableImpl<E, ID>

    override fun createTable(entityType: EntityType): TableImpl<E, ID> =
        SubQueryTableImpl(this, entityType)

    override val parentTable: Table<P, PID>
        get() = parentQuery.table

    override fun accept(visitor: TableReferenceVisitor) {
        accept(visitor, false)
    }

    override fun renderTo(builder: SqlBuilder) {
         renderTo(builder, false)
    }

    override fun <X : Any> select(
        expression: NonNullExpression<X>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X> =
        ConfigurableTypedSubQueryImpl.select(this, listOf(expression as Selection<*>))

    override fun <X : Any> select(
        expression: Expression<X>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X?> =
        ConfigurableTypedSubQueryImpl.select(this, listOf(expression as Selection<*>))

    override fun <X : Entity<XID>, XID : Comparable<XID>> select(
        table: NonNullJoinableTable<X, XID>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X> =
        ConfigurableTypedSubQueryImpl.select(this, listOf(table as Selection<*>))

    override fun <X : Entity<XID>, XID : Comparable<XID>> select(
        table: JoinableTable<X, XID>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X?> =
        ConfigurableTypedSubQueryImpl.select(this, listOf(table as Selection<*>))

    override fun <X: Any> select(
        block: ProjectionContext.() -> Projection<X>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X> =
        ConfigurableTypedSubQueryImpl.select(this, (ProjectionContext.block() as AbstractProjection).selections)
}