package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.query.TypedSubQuery
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.meta.EntityProp

interface AstVisitor {

    val sqlBuilder: SqlBuilder?
        get() = null

    fun visitTableReference(table: Table<*, *>, prop: EntityProp?) {}

    fun visitSubQuery(subQuery: TypedSubQuery<*, *, *, *, *>): Boolean = true

    fun visitAggregation(functionName: String, base: Expression<*>, prefix: String?) {}
}