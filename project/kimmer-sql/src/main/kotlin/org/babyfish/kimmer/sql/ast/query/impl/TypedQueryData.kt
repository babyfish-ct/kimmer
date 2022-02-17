package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.ast.Selection

internal data class TypedQueryData(
    val selections: List<Selection<*>>,
    val oldSelections: List<Selection<*>>? = null,
    val distinct: Boolean = false,
    val limit: Int = Int.MAX_VALUE,
    val offset: Int = 0,
    val withoutSortingAndPaging: Boolean = false
)