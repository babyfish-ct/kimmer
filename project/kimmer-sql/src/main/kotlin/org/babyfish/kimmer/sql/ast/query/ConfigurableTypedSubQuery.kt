package org.babyfish.kimmer.sql.ast.query

interface ConfigurableTypedSubQuery<R> : TypedSubQuery<R> {

    fun limit(limit: Int, offset: Int = 0): ConfigurableTypedSubQuery<R>

    fun distinct(distinct: Boolean): ConfigurableTypedSubQuery<R>
}