package org.babyfish.kimmer.sql.query

import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.common.AbstractQueryTest
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.BookStore
import java.math.BigDecimal
import kotlin.test.Test

class SubQueryTest: AbstractQueryTest() {

    @Test
    fun testColumnInSubQuery() {
        sqlClient.createQuery(Book::class) {
            where {
                table.id valueIn subQuery(Author::class) {
                    where(table.firstName eq "Alex")
                    select(table.books.id)
                }
            }
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID in (
                    |select tb_3_.BOOK_ID from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.FIRST_NAME = $1
                |)""".trimMargin()
            }
            variables("Alex")
        }
    }

    @Test
    fun testTwoColumnsInSubQuery() {
        sqlClient.createQuery(Book::class) {
            where {
                tuple {
                    table.name then
                        table.price
                } valueIn subQuery(Book::class) {
                    groupBy(table.name)
                    select {
                        table.name then
                            table.price.max().asNonNull()
                    }
                }
            }
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, 
                    |tb_1_.EDITION, 
                    |tb_1_.NAME, 
                    |tb_1_.PRICE, 
                    |tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where (
                    |tb_1_.NAME, 
                    |tb_1_.PRICE
                |) in (
                    |select 
                        |tb_2_.NAME, 
                        |max(tb_2_.PRICE
                    |) 
                    |from BOOK as tb_2_ 
                    |group by tb_2_.NAME
                |)""".trimMargin()
            }
            variables()
        }
    }

    @Test
    fun testExists() {
        sqlClient.createQuery(Book::class) {
            where {
                exists(untypedSubQuery(Author::class) {
                    where(
                        parentTable.id eq table.books.id,
                        table.firstName eq "Alex"
                    )
                })
            }
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where exists(
                    |select 1 from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where 
                        |tb_1_.ID = tb_3_.BOOK_ID 
                    |and 
                        |tb_2_.FIRST_NAME = $1
                |)""".trimMargin()
            }
            variables("Alex")
        }
    }

    @Test
    fun testExistsWithTypedQuery() {
        sqlClient.createQuery(Book::class) {
            where {
                exists(subQuery(Author::class) {
                    where(
                        parentTable.id eq table.books.id,
                        table.firstName eq "Alex"
                    )
                    select(table)
                })
            }
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where exists(
                    |select 1 from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where 
                        |tb_1_.ID = tb_3_.BOOK_ID 
                    |and 
                        |tb_2_.FIRST_NAME = $1
                |)""".trimMargin()
            }
            variables("Alex")
        }
    }

    @Test
    fun testSubQueryAsSimpleExpression() {
        sqlClient.createQuery(Book::class) {
            where {
                table.price gt subQuery(Book::class) {
                    select(coalesce(table.price.avg(), BigDecimal.ZERO))
                }
            }
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where tb_1_.PRICE > (
                    |select coalesce(avg(tb_2_.PRICE), $1) from BOOK as tb_2_
                |)""".trimMargin()
            }
            variables(BigDecimal.ZERO)
        }
    }

    @Test
    fun testSubQueryAsSelectionOrderByClause() {
        sqlClient.createQuery(BookStore::class) {
            val subQuery = subQuery(Book::class) {
                where(parentTable.id eq table.store.id)
                select(coalesce(table.price.avg(), BigDecimal.ZERO))
            }
            orderBy(subQuery, OrderMode.DESC)
            select {table then subQuery }
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, 
                    |tb_1_.NAME, 
                    |tb_1_.WEBSITE, 
                    |(
                        |select coalesce(avg(tb_2_.PRICE), $1) 
                        |from BOOK as tb_2_ 
                        |where tb_1_.ID = tb_2_.STORE_ID
                    |) 
                |from BOOK_STORE as tb_1_ 
                |order by (
                    |select coalesce(avg(tb_2_.PRICE), $2) 
                    |from BOOK as tb_2_ 
                    |where tb_1_.ID = tb_2_.STORE_ID
                |) desc""".trimMargin()
            }
            variables(BigDecimal.ZERO, BigDecimal.ZERO)
        }
    }

    @Test
    fun testSubQueryWithAny() {
       sqlClient.createQuery(Book::class) {
            where(
                table.id eq any(
                        subQuery(Author::class) {
                        where(table.firstName valueIn listOf("Alex", "Bill"))
                        select(table.books.id)
                    }
                )
            )
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID = any(
                    |select tb_3_.BOOK_ID 
                    |from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.FIRST_NAME in ($1, $2)
                |)""".trimMargin()
            }
           variables("Alex", "Bill")
       }
    }

    @Test
    fun testSubQueryWithSome() {
        sqlClient.createQuery(Book::class) {
            where(
                table.id eq some(
                    subQuery(Author::class) {
                        where(table.firstName valueIn listOf("Alex", "Bill"))
                        select(table.books.id)
                    }
                )
            )
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID = some(
                    |select tb_3_.BOOK_ID 
                    |from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.FIRST_NAME in ($1, $2)
                |)""".trimMargin()
            }
            variables("Alex", "Bill")
        }
    }

    @Test
    fun testSubQueryWithAll() {
        sqlClient.createQuery(Book::class) {
            where(
                table.id eq all(
                    subQuery(Author::class) {
                        where(table.firstName valueIn listOf("Alex", "Bill"))
                        select(table.books.id)
                    }
                )
            )
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID = all(
                    |select tb_3_.BOOK_ID 
                    |from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.FIRST_NAME in ($1, $2)
                |)""".trimMargin()
            }
            variables("Alex", "Bill")
        }
    }
}