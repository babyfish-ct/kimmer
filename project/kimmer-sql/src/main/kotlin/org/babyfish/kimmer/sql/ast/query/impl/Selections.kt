package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.Selection
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl

internal fun mergedSelections(
    typedQueryImplementor1: TypedQueryImplementor,
    typedQueryImplementor2: TypedQueryImplementor
): List<Selection<*>> {
    val list1 = typedQueryImplementor1.selections
    val list2 = typedQueryImplementor2.selections
    if (list1.size != list2.size) {
        throw IllegalArgumentException("Cannot merged sub queries with different selections")
    }
    for (index in list1.indices) {
        if (!isSameType(list1[index], list2[index])) {
            throw IllegalArgumentException("Cannot merged sub queries with different selections")
        }
    }
    return list1
}

private fun isSameType(a: Selection<*>, b: Selection<*>): Boolean =
    when (a) {
        is TableImpl<*, *> -> if (b is TableImpl<*, *>) {
            a.entityType === b.entityType
        } else {
            false
        }
        is Expression<*> -> if (b is Expression<*>) {
            a.selectedType === b.selectedType
        } else {
            false
        }
        else -> error("Internal bug: Unexpected selection type: ${a::class.qualifiedName}")
    }
