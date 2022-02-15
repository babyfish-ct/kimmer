package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.ast.table.impl.accept
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.runtime.PaginationContext

internal abstract class AbstractConfigurableTypedQueryImpl<E, ID, R>(
    val data: TypedQueryData,
    open val baseQuery: AbstractQueryImpl<E, ID>,
) : TypedQueryImplementor
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

    override val selections: List<Selection<*>>
        get() = data.selections

    override fun renderTo(builder: SqlBuilder) {
        if (data.withoutSortingAndPaging || data.limit == Int.MAX_VALUE) {
            builder.renderRenderWithoutPaging()
        } else {
            val paginationBuilder = builder.createChildBuilder()
            paginationBuilder.renderRenderWithoutPaging()
            paginationBuilder.build {
                val ctx = PaginationContext(
                    data.limit,
                    data.offset,
                    it.first,
                    it.second,
                    builder is R2dbcSqlBuilder
                )
                baseQuery.sqlClient.dialect.pagination(ctx)
                ctx.build()
            }
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        data.selections.forEach { it.accept(visitor) }
        baseQuery.accept(visitor, data.withoutSortingAndPaging)
    }

    private fun SqlBuilder.renderRenderWithoutPaging() {
        sql("select ")
        if (data.distinct) {
            sql("distinct ")
        }
        var sp: String? = null
        for (selection in data.selections) {
            if (sp === null) {
                sp = ", "
            } else {
                sql(sp)
            }
            if (selection is TableImpl<*, *>) {
                selection.renderAsSelection(this)
            } else if (selection !== null) {
                (selection as Renderable).renderTo(this)
            }
        }
        baseQuery.renderTo(this, data.withoutSortingAndPaging)
    }
}