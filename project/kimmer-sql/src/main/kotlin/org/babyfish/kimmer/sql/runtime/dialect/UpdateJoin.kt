package org.babyfish.kimmer.sql.runtime.dialect

data class UpdateJoin(
    val joinedTableUpdatable: Boolean,
    val from: From
) {
    enum class From {
        UNNECESSARY,
        AS_ROOT,
        AS_JOIN
    }
}