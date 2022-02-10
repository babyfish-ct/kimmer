package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable

interface TypedSqlQuery<T: Immutable, R>: SqlQuery<T>, Expression<R> {

    fun execute(con: java.sql.Connection): List<R>

    suspend fun execute(con: io.r2dbc.spi.Connection): List<R>
}
