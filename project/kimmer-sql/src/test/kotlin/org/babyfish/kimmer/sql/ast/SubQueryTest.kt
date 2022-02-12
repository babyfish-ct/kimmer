package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test

class SubQueryTest: AbstractTest() {

    @Test
    fun testColumnInSubQuery() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID in (
                    |select tb_3_.BOOK_ID from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.NAME = :1
                |)""".trimMargin().toOneLine(),
            "Alex"
        ) {
            where(
                table.id valueIn subQuery(Author::class) {
                    where(table.name eq "Alex")
                    select(table.books.id)
                }
            )
            select(table)
        }
    }

    @Test
    fun testTwoColumnsInSubQuery() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, 
                    |tb_1_.ID, 
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
                        |max(tb_2_.PRICE) 
                    |from BOOK as tb_2_ 
                    |group by tb_2_.NAME
                |)""".trimMargin().toOneLine()
        ) {
            where(
                tuple(table.name, table.price) valueIn subQuery(Book::class) {
                    groupBy(table.name)
                    select(table.name, table.price.max())
                }
            )
            select(table)
        }
    }

    @Test
    fun testExists() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where exists(
                    |select 1 from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where 
                        |tb_1_.ID = tb_3_.BOOK_ID 
                    |and 
                        |tb_2_.NAME = :1
                |)""".trimMargin().toOneLine(),
            "Alex"
        ) {
            where(
                exists(untypedSubQuery(Author::class) {
                    where(
                        parentTable.id eq table.books.id,
                        table.name eq "Alex"
                    )
                })
            )
            select(table)
        }
    }

    @Test
    fun testExistsWithTypedQuery() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where exists(
                    |select 1 from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where 
                        |tb_1_.ID = tb_3_.BOOK_ID 
                    |and 
                        |tb_2_.NAME = :1
                |)""".trimMargin().toOneLine(),
            "Alex"
        ) {
            where(
                exists(subQuery(Author::class) {
                    where(
                        parentTable.id eq table.books.id,
                        table.name eq "Alex"
                    )
                    select(table)
                })
            )
            select(table)
        }
    }

    @Test
    fun testSubQueryAsSimpleExpression() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ 
                |where tb_1_.PRICE > (
                    |select avg(tb_2_.PRICE) from BOOK as tb_2_
                |)""".trimMargin().toOneLine()
        ) {
            where(table.price gt subQuery(Book::class) {
                select(table.price.avg())
            })
            select(table)
        }
    }

    @Test
    fun testSubQueryAsSelectionOrderByClause() {
        testQuery(
            BookStore::class,
            """select 
                |concat(tb_1_.ID, :1, tb_1_.NAME), 
                |tb_1_.ID, 
                |tb_1_.NAME, 
                |(
                    |select avg(tb_2_.PRICE) 
                    |from BOOK as tb_2_ 
                    |where tb_1_.ID = tb_2_.STORE_ID
                |) 
                |from BOOK_STORE as tb_1_ 
                |order by (
                    |select avg(tb_2_.PRICE) 
                    |from BOOK as tb_2_ 
                    |where tb_1_.ID = tb_2_.STORE_ID
                |) desc""".trimMargin().toOneLine(),
            "-"
        ) {
            val subQuery = subQuery(Book::class) {
                where(parentTable.id eq table.store.id)
                select(table.price.avg())
            }
            orderBy(subQuery, true)
            select(table, subQuery)
        }
    }

    @Test
    fun testSubQueryWithAny() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID = any(
                    |select tb_3_.BOOK_ID 
                    |from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.NAME in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "Alex",
            "Bill"
        ) {
            where(
                table.id eq any(
                        subQuery(Author::class) {
                        where(table.name valueIn listOf("Alex", "Bill"))
                        select(table.books.id)
                    }
                )
            )
            select(table)
        }
    }

    @Test
    fun testSubQueryWithSome() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID = some(
                    |select tb_3_.BOOK_ID 
                    |from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.NAME in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "Alex",
            "Bill"
        ) {
            where(
                table.id eq some(
                    subQuery(Author::class) {
                        where(table.name valueIn listOf("Alex", "Bill"))
                        select(table.books.id)
                    }
                )
            )
            select(table)
        }
    }

    @Test
    fun testSubQueryWithAll() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                |from BOOK as tb_1_ where tb_1_.ID = all(
                    |select tb_3_.BOOK_ID 
                    |from AUTHOR as tb_2_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                    |where tb_2_.NAME in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "Alex",
            "Bill"
        ) {
            where(
                table.id eq all(
                    subQuery(Author::class) {
                        where(table.name valueIn listOf("Alex", "Bill"))
                        select(table.books.id)
                    }
                )
            )
            select(table)
        }
    }
}