package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import java.lang.IllegalStateException

internal abstract class AbstractMutableStatement(
    val tableAliasAllocator: TableAliasAllocator,
    val sqlClient: SqlClientImpl
) {
    private var frozen = false

    fun freeze() {
        frozen = true
    }

    fun validateMutable() {
        if (frozen) {
            throw IllegalStateException("Cannot mutate the query because it has been frozen")
        }
    }
}