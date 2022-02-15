package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.ast.Renderable
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceElement

internal interface TypedQueryImplementor : Renderable, TableReferenceElement {
    val selections: List<Selection<*>>
}