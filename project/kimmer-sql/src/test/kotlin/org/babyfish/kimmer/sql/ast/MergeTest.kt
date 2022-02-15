package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test

class MergeTest: AbstractTest() {

    @Test
    fun test() {
        val query =
            sqlClient.createQuery(Book::class) {
                where(
                    table.name ilike "G",
                    table.id valueIn (
                        subQuery(Author::class) {
                            where(table.fullName like "A")
                            select(table.books.id)
                        } unionAll subQuery(Author::class) {
                            where(table.fullName like "B")
                            select(table.books.id)
                        }
                    )
                )
                select(table)
            } minus sqlClient.createQuery(Book::class) {
                where(
                    table.name ilike "F",
                    table.id valueIn (
                        subQuery(Author::class) {
                            where(table.fullName like "C")
                            select(table.books.id)
                        } unionAll subQuery(Author::class) {
                            where(table.fullName like "D")
                            select(table.books.id)
                        }
                        )
                )
                select(table)
            }
        query.executeAndExpect {
            sql {
                """select 
                        |tb_1_.ID, 
                        |tb_1_.EDITION, 
                        |tb_1_.NAME, 
                        |tb_1_.PRICE, 
                        |tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                    |where lower(tb_1_.NAME) like $1 
                    |and tb_1_.ID in (
                        |(
                            |select tb_3_.BOOK_ID 
                            |from AUTHOR as tb_2_ 
                            |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                            |where concat(tb_2_.FIRST_NAME, $2, tb_2_.LAST_NAME) like $3
                        |) union all (
                            |select tb_6_.BOOK_ID from AUTHOR as tb_5_ 
                            |inner join BOOK_AUTHOR_MAPPING as tb_6_ on tb_5_.ID = tb_6_.AUTHOR_ID 
                            |where concat(tb_5_.FIRST_NAME, $4, tb_5_.LAST_NAME) like $5
                        |)
                    |) minus 
                    |select 
                        |tb_1_.ID, 
                        |tb_1_.EDITION, 
                        |tb_1_.NAME, 
                        |tb_1_.PRICE, 
                        |tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                    |where lower(tb_1_.NAME) like $6 
                    |and tb_1_.ID in (
                        |(
                            |select tb_3_.BOOK_ID from AUTHOR as tb_2_ 
                            |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                            |where concat(tb_2_.FIRST_NAME, $7, tb_2_.LAST_NAME) 
                            |like $8
                        |) union all (
                            |select tb_6_.BOOK_ID 
                            |from AUTHOR as tb_5_ 
                            |inner join BOOK_AUTHOR_MAPPING as tb_6_ on tb_5_.ID = tb_6_.AUTHOR_ID 
                            |where concat(tb_5_.FIRST_NAME, $9, tb_5_.LAST_NAME) like $10
                        |)
                    |)""".trimMargin()
            }
            variables("g", " ", "A", " ", "B", "f", " ", "C", " ", "D")
        }
    }
}