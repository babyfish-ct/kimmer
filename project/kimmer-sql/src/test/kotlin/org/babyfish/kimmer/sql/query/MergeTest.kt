package org.babyfish.kimmer.sql.query

import org.babyfish.kimmer.sql.common.AbstractQueryTest
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.like
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import kotlin.test.Test

class MergeTest: AbstractQueryTest() {

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
                        |select tb_3_.BOOK_ID 
                        |from AUTHOR as tb_2_ 
                        |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                        |where concat(tb_2_.FIRST_NAME, $2, tb_2_.LAST_NAME) like $3
                    |) minus 
                    |select 
                        |tb_1_.ID, 
                        |tb_1_.EDITION, 
                        |tb_1_.NAME, 
                        |tb_1_.PRICE, 
                        |tb_1_.STORE_ID 
                        |from BOOK as tb_1_ 
                    |where lower(tb_1_.NAME) like $4 
                    |and tb_1_.ID in (
                        |select tb_3_.BOOK_ID from AUTHOR as tb_2_ 
                        |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
                        |where concat(tb_2_.FIRST_NAME, $5, tb_2_.LAST_NAME) 
                        |like $6
                    |)""".trimMargin()
            }
            variables("%g%", " ", "%A%", "%f%", " ", "%C%")
        }
    }
}