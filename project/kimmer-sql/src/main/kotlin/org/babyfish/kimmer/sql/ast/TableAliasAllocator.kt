package org.babyfish.kimmer.sql.ast

@JvmInline
value class TableAliasAllocator(
    private val idRef: IntArray = IntArray(1)
) {
    fun allocate(): String =
        "tb_${++idRef[0]}_"
}