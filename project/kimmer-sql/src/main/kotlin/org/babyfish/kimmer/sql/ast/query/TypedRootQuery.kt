package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity

interface TypedRootQuery<E, ID, R>
    where E:
          Entity<ID>,
          ID: Comparable<ID> {

    fun execute(con: java.sql.Connection): List<R>

    suspend fun execute(con: io.r2dbc.spi.Connection): List<R>
}

