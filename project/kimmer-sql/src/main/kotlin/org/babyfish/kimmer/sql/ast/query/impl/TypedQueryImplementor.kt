package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.spi.Renderable
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.Ast

internal interface TypedQueryImplementor : Renderable, Ast {
    val selections: List<Selection<*>>
}