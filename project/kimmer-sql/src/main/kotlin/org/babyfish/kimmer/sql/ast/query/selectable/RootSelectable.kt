package org.babyfish.kimmer.sql.ast.query.selectable

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedRootQuery
import org.babyfish.kimmer.sql.ast.table.JoinableTable
import org.babyfish.kimmer.sql.ast.table.NonNullJoinableTable

interface RootSelectable<E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullJoinableTable<E, ID>

    fun <X: Any> select(
        expression: NonNullExpression<X>
    ): ConfigurableTypedRootQuery<E, ID, X>

    fun <X: Any> select(
        expression: Expression<X>
    ): ConfigurableTypedRootQuery<E, ID, X?>

    fun <X: Entity<XID>, XID: Comparable<XID>> select(
        table: NonNullJoinableTable<X, XID>
    ): ConfigurableTypedRootQuery<E, ID, X>

    fun <X: Entity<XID>, XID: Comparable<XID>> select(
        table: JoinableTable<X, XID>
    ): ConfigurableTypedRootQuery<E, ID, X?>

    /**
     * select {
     *      expr1 then
     *      expr2 then
     *      ...
     *      exprN
     * }
     */
    fun <X: Any> select(
        block: ProjectionContext.() -> Projection<X>
    ): ConfigurableTypedRootQuery<E, ID, X>
}