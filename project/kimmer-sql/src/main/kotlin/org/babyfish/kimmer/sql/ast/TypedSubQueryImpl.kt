package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity

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
        baseQuery.renderWithoutSelection(builder)
        builder.sql(")")
    }

    override fun accept(visitor: TableReferenceVisitor) {
        selections.forEach { it.accept(visitor) }
    }
}