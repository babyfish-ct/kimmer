package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.*
import org.babyfish.kimmer.sql.ast.model.Author
import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.BookStore
import kotlin.test.Test

class ContainsTest: AbstractTest() {

    @Test
    fun testOneToMany() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as tb_1_ 
                |where tb_1_.ID in (
                    |select STORE_ID from BOOK where ID in ($1, $2)
                |)""".trimMargin().toOneLine(),
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(table.listContains(BookStore::books, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }
    }

    @Test
    fun testNormalManyToManyByNormalApi() {
        testQuery(
            Book::class,
            """select 1 from BOOK as tb_1_ where 
                |tb_1_.ID in (
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in ($1, $2)
                |)""".trimMargin().toOneLine(),
            alexId,
            danId
        ) {
            where(table.listContains(Book::authors, listOf(alexId, danId)))
            select(constant(1))
        }
    }

    @Test
    fun testInverseManyToManyByNormalApi() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID in (
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin().trimMargin().toOneLine(),
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(table.listContains(Author::books, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }
    }

    @Test
    fun testNormalManyToManyByInverseApi() {
        testQuery(
            Book::class,
            """select 1 from BOOK as tb_1_ where 
                |tb_1_.ID in (
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in ($1, $2)
                |)""".trimMargin().toOneLine(),
            alexId,
            danId
        ) {
            where(table.`←listContains`(Author::books, listOf(alexId, danId)))
            select(constant(1))
        }
    }

    @Test
    fun testInverseManyToManyByInverseApi() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as tb_1_ 
                |where tb_1_.ID in (
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in ($1, $2)
                |)""".trimMargin().toOneLine(),
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(table.`←listContains`(Book::authors, listOf(learningGraphQLId1, learningGraphQLId2)))
            select(constant(1))
        }
    }
}