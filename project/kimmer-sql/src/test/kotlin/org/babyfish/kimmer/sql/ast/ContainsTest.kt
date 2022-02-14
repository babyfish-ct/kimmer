package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.*
import org.babyfish.kimmer.sql.ast.model.Author
import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.BookStore
import kotlin.test.Test

class ContainsTest: AbstractTest() {

    @Test
    fun testOneToMany() {
        sqlClient.createQuery(BookStore::class) {
            where(table.listContains(BookStore::books, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                |where tb_1_.ID in (
                    |select STORE_ID from BOOK where ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun testNormalManyToManyByNormalApi() {
        sqlClient.createQuery(Book::class) {
            where(table.listContains(Book::authors, listOf(alexId, danId)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ where 
                |tb_1_.ID in (
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(alexId, danId)
        }
    }

    @Test
    fun testInverseManyToManyByNormalApi() {
        sqlClient.createQuery(Author::class) {
            where(table.listContains(Author::books, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID in (
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun testNormalManyToManyByInverseApi() {
        sqlClient.createQuery(Book::class) {
            where(table.`←listContains`(Author::books, listOf(alexId, danId)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                    |where tb_1_.ID in (
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(alexId, danId)
        }
    }

    @Test
    fun testInverseManyToManyByInverseApi() {
        sqlClient.createQuery(Author::class) {
            where(table.`←listContains`(Book::authors, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID in (
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }
}