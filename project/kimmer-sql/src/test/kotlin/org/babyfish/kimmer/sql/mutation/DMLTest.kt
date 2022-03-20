package org.babyfish.kimmer.sql.mutation

import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.common.AbstractMutationTest
import org.babyfish.kimmer.sql.common.graphQLInActionId1
import org.babyfish.kimmer.sql.common.graphQLInActionId2
import org.babyfish.kimmer.sql.common.graphQLInActionId3
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.runtime.dialect.MysqlDialect
import org.babyfish.kimmer.sql.runtime.dialect.PostgresDialect
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.Ignore
import kotlin.test.expect

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
    fun testUpdateWithNullArgument() {
        sqlClient.createUpdate(BookStore::class) {
            set(table.website, nullValue(String::class))
        }.executeAndExpectRowCount {
            statement {
                sql("update BOOK_STORE tb_1_ set WEBSITE = $1")
                variables(DbNull(String::class))
            }
            rowCount(2)
        }
    }

    @Test
    fun testUpdateJoinByMySql() {

        assumeNativeDatabase()

        using(MysqlDialect()) {
            sqlClient.createUpdate(Author::class) {
                set(table.firstName, concat(table.firstName, value("*")))
                set(table.books.name, concat(table.books.name, value("*")))
                set(table.books.store.name, concat(table.books.store.name, value("*")))
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
                            |set 
                            |tb_1_.FIRST_NAME = concat(tb_1_.FIRST_NAME, $1), 
                            |tb_3_.NAME = concat(tb_3_.NAME, $2), 
                            |tb_4_.NAME = concat(tb_4_.NAME, $3) 
                            |where tb_4_.NAME = $4""".trimMargin()
                    }
                    variables("*", "*", "*", "MANNING")
                }
                rowCount(5)
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

    @Test
    fun testUpdateJoinByPostgres() {

        assumeNativeDatabase()

        using(PostgresDialect()) {
            sqlClient.createUpdate(Author::class) {
                set(table.firstName, concat(table.firstName, value("*")))
                where(table.books.store.name eq "MANNING")
            }.executeAndExpectRowCount(
                POSTGRES_DATA_SOURCE,
                POSTGRES_CONNECTION_FACTORY
            ) {
                statement {
                    sql{
                        """update AUTHOR tb_1_ 
                            |set FIRST_NAME = concat(tb_1_.FIRST_NAME, $1) 
                            |from BOOK_AUTHOR_MAPPING as tb_2_ 
                            |inner join BOOK as tb_3_ on tb_2_.BOOK_ID = tb_3_.ID 
                            |inner join BOOK_STORE as tb_4_ on tb_3_.STORE_ID = tb_4_.ID 
                            |where tb_1_.ID = tb_2_.AUTHOR_ID 
                            |and tb_4_.NAME = $2""".trimMargin()
                    }
                    variables("*", "MANNING")
                }
                rowCount(1)
            }
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

    @Test
    fun testDelete() {
        sqlClient.createDelete(Book::class) {
            where(table.name eq "Learning GraphQL")
        }.executeAndExpectRowCount {
            statement {
                sql("delete from BOOK as tb_1_ where tb_1_.NAME = $1")
                variables("Learning GraphQL")
            }
            rowCount(3)
        }
    }

    @Test
    fun testDeleteWithJoin() {
        sqlClient.createDelete(Book::class) {
            where(table.store.name eq "MANNING")
        }.executeAndExpectRowCount {
            statement {
                sql {
                    """select distinct tb_1_.ID 
                        |from BOOK as tb_1_ 
                        |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                        |where tb_2_.NAME = $1""".trimMargin()
                }
                variables("MANNING")
            }
            statement {
                sql("select BOOK_ID, AUTHOR_ID from BOOK_AUTHOR_MAPPING where BOOK_ID in ($1, $2, $3)")
                variables {
                    expect(3) { size }
                    expect(true) { graphQLInActionId1 in this }
                    expect(true) { graphQLInActionId2 in this }
                    expect(true) { graphQLInActionId3 in this }
                }
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
            }
            statement {
                sql("delete from BOOK_AUTHOR_MAPPING where BOOK_ID = $1")
            }
            statement {
                sql {
                    """select tb_1_.BOOK_ID, tb_1_.ID, tb_1_.BOOK_ID, tb_1_.NAME 
                        |from CHAPTER as tb_1_ 
                        |where tb_1_.BOOK_ID in ($1, $2, $3)""".trimMargin()
                }
                variables {
                    expect(3) { size }
                    expect(true) { graphQLInActionId1 in this }
                    expect(true) { graphQLInActionId2 in this }
                    expect(true) { graphQLInActionId3 in this }
                }
            }
            statement {
                sql("delete from BOOK where ID in ($1, $2, $3)")
                variables {
                    expect(3) { size }
                    expect(true) { graphQLInActionId1 in this }
                    expect(true) { graphQLInActionId2 in this }
                    expect(true) { graphQLInActionId3 in this }
                }
            }
            rowCount(3)
        }
    }
}