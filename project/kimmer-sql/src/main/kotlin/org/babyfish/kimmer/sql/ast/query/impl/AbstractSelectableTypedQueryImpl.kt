package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.query.SelectableTypedRootQuery
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.babyfish.kimmer.sql.ast.table.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.accept
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp

internal abstract class AbstractSelectableTypedQueryImpl<E, ID, R>(
    val data: TypedQueryData,
    open val baseQuery: AbstractQueryImpl<E, ID>,
) : Renderable,
    TableReferenceElement
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("select ")
        var sp: String? = null
        for (selection in data.selections) {
            if (sp === null) {
                sp = ", "
            } else {
                builder.sql(sp)
            }
            if (selection is TableImpl<*, *>) {
                selection.renderAsSelection(builder)
            } else if (selection !== null) {
                (selection as Renderable).renderTo(builder)
            }
        }
        baseQuery.renderTo(builder, data.withoutSortingAndPaging)
        data.apply {
            if (!withoutSortingAndPaging) {
                builder.apply {
                    if (limit != Int.MAX_VALUE) {
                        sql(" limit ")
                        variable(limit)
                    }
                    if (offset > 0) {
                        sql(" offset ")
                        variable(offset)
                    }
                }
            }
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        data.selections.forEach { it.accept(visitor) }
        baseQuery.accept(visitor, data.withoutSortingAndPaging)
    }
}