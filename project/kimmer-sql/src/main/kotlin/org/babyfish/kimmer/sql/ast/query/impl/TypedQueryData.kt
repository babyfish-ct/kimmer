package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.ast.Selection

data class TypedQueryData(
    val selections: List<Selection<*>>,
    val limit: Int = Int.MAX_VALUE,
    val offset: Int = 0,
    val withoutSortingAndPaging: Boolean = false
)