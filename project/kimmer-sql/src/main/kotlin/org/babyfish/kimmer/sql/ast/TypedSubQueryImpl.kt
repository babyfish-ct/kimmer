package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable

internal class TypedSubQueryImpl<P: Immutable, T: Immutable, R>(
    private val selections: List<Selection<*>>,
    private val baseQuery: SubQueryImpl<P, T>
): TypedSqlSubQuery<P, T, R>, SqlSubQuery<P, T> by (baseQuery), Renderable {

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("(select ")
        var sp: String? = null
        for (expression in selections) {
            if (sp !== null) {
                builder.sql(sp)
            } else {
                sp = ", "
            }
            (expression as Renderable).renderTo(builder)
        }
        baseQuery.renderWithoutSelection(builder)
        builder.sql(")")
    }
}