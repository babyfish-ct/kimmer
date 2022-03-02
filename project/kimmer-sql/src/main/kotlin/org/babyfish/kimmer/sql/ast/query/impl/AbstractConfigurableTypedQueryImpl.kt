package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.spi.Renderable
import org.babyfish.kimmer.sql.ast.AstVisitor
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.runtime.PaginationContext

internal abstract class AbstractConfigurableTypedQueryImpl<E, ID, R>(
    val data: TypedQueryData,
    baseQuery: AbstractMutableQueryImpl<E, ID>,
) : TypedQueryImplementor
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    private val _baseQuery: AbstractMutableQueryImpl<E, ID>

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
        baseQuery.freeze()
        _baseQuery = baseQuery
    }

    open val baseQuery: AbstractMutableQueryImpl<E, ID>
        get() = _baseQuery

    override val selections: List<Selection<*>>
        get() = data.selections

    override fun renderTo(builder: SqlBuilder) {
        if (data.withoutSortingAndPaging || data.limit == Int.MAX_VALUE) {
            builder.renderRenderWithoutPaging()
        } else {
            val paginationBuilder = (builder as AbstractSqlBuilder).createChildBuilder()
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

    override fun accept(visitor: AstVisitor) {
        data.selections.forEach {
            (it as Ast).accept(visitor)
        }
        baseQuery.accept(visitor, data.oldSelections, data.withoutSortingAndPaging)
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
            when (selection) {
                is TableImpl<*, *> -> selection.renderAsSelection(this)
                is Renderable -> selection.renderTo(this)
                else -> error("Internal bug unexpected selection")
            }
        }
        baseQuery.renderTo(this, data.withoutSortingAndPaging)
    }
}