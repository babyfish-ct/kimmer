package org.babyfish.kimmer.sql.ast

interface Executable<R> {

    fun execute(con: java.sql.Connection): R

    suspend fun execute(con: io.r2dbc.spi.Connection): R
}