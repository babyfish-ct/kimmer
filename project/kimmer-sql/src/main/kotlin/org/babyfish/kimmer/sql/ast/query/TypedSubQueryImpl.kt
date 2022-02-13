package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.Selection
import org.babyfish.kimmer.sql.ast.NonNullExpression
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.TypedSqlSubQuery
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.ast.table.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.accept

internal class TypedSubQueryImpl<P, PID, E, ID, R>(
    private val selections: List<Selection<*>>,
    private val baseQuery: SubQueryImpl<P, PID, E, ID>
): TypedSqlSubQuery<P, PID, E, ID, R>,
    SqlSubQuery<P, PID, E, ID> by (baseQuery),
    Renderable,
    TableReferenceElement
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("(select ")
        var sp: String? = null
        for (selection in selections) {
            if (sp !== null) {
                builder.sql(sp)
            } else {
                sp = ", "
            }
            if (selection is TableImpl<*, *>) {
                selection.renderAsSelection(builder)
            } else {
                (selection as Renderable).renderTo(builder)
            }
        }
        baseQuery.renderTo(builder)
        builder.sql(")")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        if (!visitor.skipSubQuery()) {
            selections.forEach { it.accept(visitor) }
            baseQuery.accept(visitor)
        }
    }
}