package org.babyfish.kimmer.sql.ast.query.selectable

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.ConfigurableTypedSubQuery
import org.babyfish.kimmer.sql.ast.table.JoinableTable
import org.babyfish.kimmer.sql.ast.table.NonNullJoinableTable
import org.babyfish.kimmer.sql.ast.table.NonNullSubQueryTable

interface SubSelectable<P: Entity<PID>, PID: Comparable<PID>, E: Entity<ID>, ID: Comparable<ID>> {

    val table: NonNullSubQueryTable<E, ID>

    fun <X: Any> select(
        expression: NonNullExpression<X>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X>

    fun <X: Any> select(
        expression: Expression<X>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X?>

    fun <X: Entity<XID>, XID: Comparable<XID>> select(
        table: NonNullJoinableTable<X, XID>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X>

    fun <X: Entity<XID>, XID: Comparable<XID>> select(
        table: JoinableTable<X, XID>
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X?>

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
    ): ConfigurableTypedSubQuery<P, PID, E, ID, X>
}