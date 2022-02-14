package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceElement
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.accept
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl

internal abstract class AbstractSelectableTypedQueryImpl<E, ID, R>(
    val data: TypedQueryData,
    open val baseQuery: AbstractQueryImpl<E, ID>,
) : Renderable,
    TableReferenceElement
    where E:
          Entity<ID>,
          ID: Comparable<ID>,
          R: Any {

    init {
        data.selections.forEach {
            when (it) {
                is TableImpl<*, *> -> {}
                is Expression<*> -> if (!it.isSelectable) {
                    throw IllegalArgumentException(
                        "Expression '${it::class.qualifiedName}' is not selectable"
                    )
                } else ->
                    throw IllegalArgumentException(
                        "Expression '${it::class.qualifiedName}' is not selectable"
                    )
            }
        }
    }

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