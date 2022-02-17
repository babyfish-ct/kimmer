package org.babyfish.kimmer.sql.ast.table.impl

internal class TableAliasAllocator {

    private var tableIdSequence = 0

    fun allocate(): String =
        "tb_${++tableIdSequence}_"
}