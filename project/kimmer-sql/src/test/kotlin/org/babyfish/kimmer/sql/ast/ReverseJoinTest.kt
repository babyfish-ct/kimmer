package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.*
import org.babyfish.kimmer.sql.ast.model.*
import java.util.*
import kotlin.test.Test

class ReverseJoinTest: AbstractTest() {

    @Test
    fun testReverseJoinOnInverseProp() {
        sqlClient.createQuery(Book::class) {
            where(table.`←joinList`(Author::books).firstName eq "Alex")
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.BOOK_ID 
                    |inner join AUTHOR as tb_3_ on tb_2_.AUTHOR_ID = tb_3_.ID 
                    |where tb_3_.FIRST_NAME = $1""".trimMargin()
            }
            variables("Alex")
        }
    }

    @Test
    fun testReverseJoinOnNormalProp() {
        sqlClient.createQuery(Author::class) {
            where(table.`←joinList`(Book::authors).name eq "Learning GraphQL")
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                    |inner join BOOK as tb_3_ on tb_2_.BOOK_ID = tb_3_.ID 
                    |where tb_3_.NAME = $1""".trimMargin()
            }
            variables("Learning GraphQL")
        }
    }

    @Test
    fun testReverseHalfJoinOnInverseProp() {
        sqlClient.createQuery(Book::class) {
            where(table.`←joinList`(Author::books).id valueIn listOf(alexId, danId))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.BOOK_ID 
                    |where tb_2_.AUTHOR_ID in ($1, $2)""".trimMargin()
            }
            variables(alexId, danId)
        }
    }

    @Test
    fun testReverseHalfJoinOnNormalProp() {
        sqlClient.createQuery(Author::class) {
            where {
                table.`←joinList`(Book::authors).id valueIn
                    listOf(learningGraphQLId1, learningGraphQLId2)
            }
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                    |where tb_2_.BOOK_ID in ($1, $2)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun mergeNormalJoinsAndReversedJoins() {
        sqlClient.createQuery(BookStore::class) {
            where {
                or(
                    table
                        .`←joinReference?`(Book::store)
                        .`←joinList?`(Author::books)
                        .get(Author::firstName) eq "Alex",
                    table.`books?`.`authors?`.firstName eq "Tim"
                )
            }
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                    |left join BOOK as tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
                    |left join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.BOOK_ID 
                    |left join AUTHOR as tb_4_ on tb_3_.AUTHOR_ID = tb_4_.ID 
                    |where tb_4_.FIRST_NAME = $1 or tb_4_.FIRST_NAME = $2""".trimMargin()
            }
            variables("Alex", "Tim")
        }
    }

    @Test
    fun mergeNormalJoinsAndReversedJoinsWithDiffJoinTypes() {
        sqlClient.createQuery(BookStore::class) {
            where {
                or(
                    table
                        .`←joinReference?`(Book::store)
                        .`←joinList?`(Author::books)
                        .get(Author::firstName) eq "Alex",
                    table.books.authors.firstName eq "Tim"
                )
            }
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                    |inner join BOOK as tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.BOOK_ID 
                    |inner join AUTHOR as tb_4_ on tb_3_.AUTHOR_ID = tb_4_.ID 
                    |where tb_4_.FIRST_NAME = $1 or tb_4_.FIRST_NAME = $2""".trimMargin()
            }
            variables("Alex", "Tim")
        }
    }
}