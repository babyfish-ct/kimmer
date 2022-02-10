package org.babyfish.kimmer.sql.meta.config

import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.JoinableTable

interface Formula: Storage {
    fun get(table: JoinableTable<*>): Expression<*>
}