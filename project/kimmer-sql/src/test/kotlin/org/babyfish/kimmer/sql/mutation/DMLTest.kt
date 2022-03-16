package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.ast.eq
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.common.AbstractMutationTest
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.runtime.dialect.MysqlDialect
import org.babyfish.kimmer.sql.runtime.dialect.PostgresDialect
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.Ignore

class DMLTest: AbstractMutationTest() {

    @Test
    fun testUpdate() {
        sqlClient.createUpdate(Author::class) {
            set(table.firstName, concat(table.firstName, value("*")))
            where(table.firstName eq "Dan")
        }.executeAndExpectRowCount {
            statement {
                sql("update AUTHOR tb_1_ set FIRST_NAME = concat(tb_1_.FIRST_NAME, $1) where tb_1_.FIRST_NAME = $2")
                variables("*", "Dan")
            }
            rowCount(1)
        }
    }

    @Test
    fun testUpdateJoinByMySql() {
        using(MysqlDialect()) {
            sqlClient.createUpdate(Author::class) {
                set(table.firstName, concat(table.firstName, value("*")))
                where(table.books.store.name eq "MANNING")
            }.executeAndExpectRowCount(
                MYSQL_DATA_SOURCE,
                MYSQL_CONNECTION_FACTORY
            ) {
                statement {
                    sql {
                        """update AUTHOR tb_1_ 
                            |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                            |inner join BOOK as tb_3_ on tb_2_.BOOK_ID = tb_3_.ID 
                            |inner join BOOK_STORE as tb_4_ on tb_3_.STORE_ID = tb_4_.ID 
                            |set tb_1_.FIRST_NAME = concat(tb_1_.FIRST_NAME, $1) 
                            |where tb_4_.NAME = $2""".trimMargin()
                    }
                    variables("*", "MANNING")
                }
                rowCount(1)
            }
        }
    }

    @Ignore
    @Test
    fun testUpdateJoinBySqlServer() {
        sqlClient.createUpdate(Author::class) {
            set(table.firstName, concat(table.firstName, value("*")))
            where(table.books.store.name eq "MANNING")
        }.executeAndExpectRowCount {

        }
    }

    @Ignore
    @Test
    fun testUpdateJoinByPostgres() {
        sqlClient.createUpdate(Author::class) {
            set(table.firstName, concat(table.firstName, value("*")))
            where(table.books.store.name eq "MANNING")
        }.executeAndExpectRowCount {

        }
    }

    @Test
    fun testUpdateJoinByPostgresErrorByOuterJoin() {
        using(PostgresDialect()) {
            sqlClient.createUpdate(Author::class) {
                set(table.firstName, concat(table.firstName, value("*")))
                where(table.`books?`.store.name eq "MANNING")
            }.executeAndExpectRowCount {
                throwable {
                    type(ExecutionException::class)
                    message {
                        """The first level table joins cannot be outer join 
                            |because current dialect 'org.babyfish.kimmer.sql.common.DynamicDialect' 
                            |indicates that the first level table joins in update statement must be 
                            |rendered as 'from' clause, but there is a first level table join whose 
                            |join type is outer: 'Author.books?'.""".trimMargin()
                    }
                }
            }
        }
    }

    @Test
    fun testUpdateJoinErrorByUpdateOtherTable() {
        throwable(
            IllegalArgumentException::class,
            """The current dialect 'org.babyfish.kimmer.sql.common.DynamicDialect' 
                |indicates that only the columns of current table can be updated""".trimMargin()
        ) {
            sqlClient.createUpdate(Author::class) {
                set(table.books.name, concat(table.books.name, value("*")))
                where(table.books.store.name eq "MANNING")
            }
        }
    }

    @Ignore
    @Test
    fun testDeleteWithJoinByMySql() {
        using(MysqlDialect()) {
            sqlClient.createDelete(Book::class) {
                where(table.store.name eq "MANNING")
            }.executeAndExpectRowCount(
                MYSQL_DATA_SOURCE,
                MYSQL_CONNECTION_FACTORY
            ) {
                statement {
                    sql {
                        """delete from BOOK as tb_1_ 
                            |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                            |where tb_2_.NAME = $1""".trimMargin()
                    }
                }
                rowCount(1)
            }
        }
    }
}