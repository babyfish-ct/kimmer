package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.*
import org.babyfish.kimmer.sql.runtime.dialect.MysqlDialect
import org.babyfish.kimmer.sql.runtime.dialect.PostgresDialect
import org.babyfish.kimmer.sql.runtime.dialect.SqlServerDialect
import org.junit.Test
import kotlin.test.assertFailsWith

class DMLTest: AbstractTest() {

    @Test
    fun testUpdate() {
        jdbc {
            sqlClient.createUpdate(Author::class) {
                set(table.firstName, concat(table.firstName, value("*")))
                where(table.firstName eq "Dan")
            }.execute(this)
        }
    }

    @Test
    fun testUpdateJoinByMySql() {
        using(MysqlDialect()) {
            jdbc {
                sqlClient.createUpdate(Author::class) {
                    set(table.firstName, concat(table.firstName, value("*")))
                    where(table.books.store.name eq "MANNING")
                }.execute(this)
            }
        }
    }

    @Test
    fun testUpdateJoinBySqlServer() {
        using(SqlServerDialect()) {
            jdbc {
                sqlClient.createUpdate(Author::class) {
                    set(table.firstName, concat(table.firstName, value("*")))
                    where(table.books.store.name eq "MANNING")
                }.execute(this)
            }
        }
    }

    @Test
    fun testUpdateJoinByPostgres() {
        using(PostgresDialect()) {
            jdbc {
                sqlClient.createUpdate(Author::class) {
                    set(table.firstName, concat(table.firstName, value("*")))
                    where(table.books.store.name eq "MANNING")
                }.execute(this)
            }
        }
    }

    @Test
    fun testUpdateJoinByPostgresErrorByOuterJoin() {
        using(PostgresDialect()) {
            jdbc {
                assertFailsWith(ExecutionException::class) {
                    sqlClient.createUpdate(Author::class) {
                        set(table.firstName, concat(table.firstName, value("*")))
                        where(table.`books?`.store.name eq "MANNING")
                    }.execute(this)
                }
            }
        }
    }

    @Test
    fun testUpdateJoinByPostgresErrorByUpdateOtherTable() {
        using(PostgresDialect()) {
            jdbc {
                assertFailsWith(IllegalArgumentException::class) {
                    sqlClient.createUpdate(Author::class) {
                        set(table.books.name, concat(table.books.name, value("*")))
                        where(table.books.store.name eq "MANNING")
                    }.execute(this)
                }
            }
        }
    }

    @Test
    fun testDelete() {
        jdbc {
            sqlClient.createDelete(Book::class) {
                where(table.store.name eq "MANNING")
            }.execute(this)
        }
    }
}