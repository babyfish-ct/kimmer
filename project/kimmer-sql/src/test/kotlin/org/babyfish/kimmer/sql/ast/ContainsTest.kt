package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.*
import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test

class ContainsTest: AbstractTest() {

    @Test
    fun testOneToMany() {

        sqlClient.createQuery(BookStore::class) {
            where(table `books ∩` listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                |where tb_1_.ID = any(
                    |select STORE_ID from BOOK where ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }

        sqlClient.createQuery(BookStore::class) {
            where(table `books ∋` listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                |where tb_1_.ID = all(
                    |select STORE_ID from BOOK where ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun testNormalManyToManyByNormalApi() {

        sqlClient.createQuery(Book::class) {
            where(table `authors ∩` listOf(alexId, danId))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ where 
                |tb_1_.ID = any(
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(alexId, danId)
        }

        sqlClient.createQuery(Book::class) {
            where(table `authors ∋` listOf(alexId, danId))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ where 
                |tb_1_.ID = all(
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
            where(
                table `books ∩` listOf(learningGraphQLId1, learningGraphQLId2)
            )
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID = any(
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }

        sqlClient.createQuery(Author::class) {
            where(
                table `books ∋` listOf(learningGraphQLId1, learningGraphQLId2)
            )
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID = all(
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
            where(table.`←listContainsAny`(Author::books, listOf(alexId, danId)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                    |where tb_1_.ID = any(
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(alexId, danId)
        }

        sqlClient.createQuery(Book::class) {
            where(table.`←listContainsAll`(Author::books, listOf(alexId, danId)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                    |where tb_1_.ID = all(
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
            where(table.`←listContainsAny`(Book::authors, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID = any(
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }

        sqlClient.createQuery(Author::class) {
            where(table.`←listContainsAll`(Book::authors, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID = all(
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }
}