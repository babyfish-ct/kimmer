package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor

internal class MergedTypedSubQueryImpl<P, PID, E, ID, R>(
    private val operator: String,
    left: TypedSubQuery<P, PID, E, ID, R>,
    right: TypedSubQuery<P, PID, E, ID, R>,
) :
    TypedSubQuery<P, PID, E, ID, R>,
    TypedQueryImplementor
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID>,
        R: Any {

    private val _selectedType: Class<R>?

    private val left: TypedQueryImplementor

    private val right: TypedQueryImplementor

    init {
        this.left = left as TypedQueryImplementor
        this.right = right as TypedQueryImplementor
        mergedSelections(left, right)
        _selectedType =
            if (left.isSelectable && right.isSelectable) {
                if (left.selectedType !== right.selectedType) {
                    throw IllegalArgumentException("Cannot merge two sub queries with different selected type")
                }
                left.selectedType
            } else {
                null
            }
    }

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("(")
        left.renderTo(builder)
        builder.sql(" ")
        builder.sql(operator)
        builder.sql(" ")
        right.renderTo(builder)
        builder.sql(")")
    }

    override val selections: List<Selection<*>>
        get() = left.selections

    override val isSelectable: Boolean
        get() = _selectedType !== null

    override val selectedType: Class<R>
        get() = _selectedType ?: error("Cannot get the selectable type of merged sub query")

    override fun accept(visitor: TableReferenceVisitor) {
        left.accept(visitor)
        right.accept(visitor)
    }

    override fun union(
        right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R> =
        MergedTypedSubQueryImpl("union", this, right)

    override fun unionAll(
        right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R> =
        MergedTypedSubQueryImpl("union all", this, right)

    override fun minus(
        right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R> =
        MergedTypedSubQueryImpl("minus", this, right)

    override fun intersect(
        right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R> =
        MergedTypedSubQueryImpl("intersect", this, right)
}