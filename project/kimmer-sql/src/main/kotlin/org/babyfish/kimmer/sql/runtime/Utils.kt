package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityProp

internal const val JDBC_BASE_INDEX = 1

internal const val R2DBC_BASE_INDEX = 0

internal fun propEqual(
    prop: EntityProp,
    entity1: Entity<*>,
    entity2: Entity<*>
): Boolean {
    val v1 = Immutable.get(entity1, prop.immutableProp)
    val v2 = Immutable.get(entity2, prop.immutableProp)
    if (prop.isReference) {
        if (v1 === v2) {
            return true
        }
        if (v1 === null || v2 === null) {
            return false
        }
        return (v1 as Entity<*>).id == (v2 as Entity<*>).id
    } else {
        return v1 == v2
    }
}

internal fun <E> List<E>.toLimitString(
    limit: Int = 10,
    transform: ((E) -> CharSequence)? = null
): String {
    val list = if (size > limit) {
        subList(0, limit)
    } else {
        this
    }
    return list.joinToString(
        prefix = "[",
        postfix = if (size > limit) ", ...]" else "]",
        transform = transform
    )
}