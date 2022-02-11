package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity

interface TypedSqlQuery<E, ID, R>:
    SqlQuery<E, ID>,
    Expression<R>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    fun execute(con: java.sql.Connection): List<R>

    suspend fun execute(con: io.r2dbc.spi.Connection): List<R>
}
