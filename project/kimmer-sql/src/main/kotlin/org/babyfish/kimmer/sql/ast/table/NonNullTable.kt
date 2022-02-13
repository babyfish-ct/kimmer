package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.NonNullExpression
import kotlin.reflect.KProperty1

interface NonNullTable<E: Entity<ID>, ID: Comparable<ID>> : Table<E, ID> {

    override val id: NonNullExpression<ID>

    override fun <X: Any> get(prop: KProperty1<E, X>): NonNullExpression<X>
}