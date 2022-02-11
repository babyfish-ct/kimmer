package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.model.Author
import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.BookStore
import org.junit.Test

class ContainsTest: AbstractTest() {

    @Test
    fun testOneToMany() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as table_1 
                |where table_1.ID in (
                    |select STORE_ID from BOOK where ID in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "id1",
            "id2"
        ) {
            where(table.listContains(BookStore::books, listOf("id1", "id2")))
            select(constant(1))
        }
    }

    @Test
    fun testNormalManyToManyByNormalApi() {
        testQuery(
            Book::class,
            """select 1 from BOOK as table_1 where 
                |table_1.ID in (
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "id1",
            "id2"
        ) {
            where(table.listContains(Book::authors, listOf("id1", "id2")))
            select(constant(1))
        }
    }

    @Test
    fun testInverseManyToManyByNormalApi() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as table_1 
                |where table_1.ID in (
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in (:1, :2)
                |)""".trimMargin().trimMargin().toOneLine(),
            "id1",
            "id2"
        ) {
            where(table.listContains(Author::books, listOf("id1", "id2")))
            select(constant(1))
        }
    }

    @Test
    fun testNormalManyToManyByInverseApi() {
        testQuery(
            Book::class,
            """select 1 from BOOK as table_1 where 
                |table_1.ID in (
                    |select BOOK_ID from BOOK_AUTHOR_MAPPING 
                    |where AUTHOR_ID in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "id1",
            "id2"
        ) {
            where(table.`~listContains`(Author::books, listOf("id1", "id2")))
            select(constant(1))
        }
    }

    @Test
    fun testInverseManyToManyByInverseApi() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as table_1 
                |where table_1.ID in (
                    |select AUTHOR_ID from BOOK_AUTHOR_MAPPING 
                    |where BOOK_ID in (:1, :2)
                |)""".trimMargin().toOneLine(),
            "id1",
            "id2"
        ) {
            where(table.`~listContains`(Book::authors, listOf("id1", "id2")))
            select(constant(1))
        }
    }
}