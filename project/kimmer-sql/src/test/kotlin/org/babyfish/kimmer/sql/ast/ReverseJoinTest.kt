package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.model.*
import org.junit.Test

class ReverseJoinTest: AbstractTest() {

    @Test
    fun testReverseJoinOnInverseProp() {
        testQuery(
            Book::class,
            """select 1 from BOOK as table_1 
                |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.BOOK_ID 
                |inner join AUTHOR as table_3 on table_2.AUTHOR_ID = table_3.ID 
                |where table_3.NAME = :1""".trimMarginToOneLine(),
            "Alex"
        ) {
            where(table.reverseJoinList(Author::books).name eq "Alex")
            select(constant(1))
        }
    }

    @Test
    fun testReverseJoinOnNormalProp() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as table_1 
                |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.AUTHOR_ID 
                |inner join BOOK as table_3 on table_2.BOOK_ID = table_3.ID 
                |where table_3.NAME = :1""".trimMarginToOneLine(),
            "Learning GraphQL"
        ) {
            where(table.reverseJoinList(Book::authors).name eq "Learning GraphQL")
            select(constant(1))
        }
    }

    @Test
    fun testReverseHalfJoinOnInverseProp() {
        testQuery(
            Book::class,
            """select 1 from BOOK as table_1 
                |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.BOOK_ID 
                |where table_2.AUTHOR_ID in (:1, :2)""".trimMarginToOneLine(),
            "id1",
            "id2"
        ) {
            where(table.reverseJoinList(Author::books).id valueIn listOf("id1", "id2"))
            select(constant(1))
        }
    }

    @Test
    fun testReverseHalfJoinOnNormalProp() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as table_1 
                |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.AUTHOR_ID 
                |where table_2.BOOK_ID in (:1, :2)""".trimMarginToOneLine(),
            "id1",
            "id2"
        ) {
            where(table.reverseJoinList(Book::authors).id valueIn listOf("id1", "id2"))
            select(constant(1))
        }
    }

    @Test
    fun mergeNormalJoinsAndReversedJoins() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as table_1 
                |left join BOOK as table_2 on table_1.ID = table_2.STORE_ID 
                |left join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.BOOK_ID 
                |left join AUTHOR as table_4 on table_3.AUTHOR_ID = table_4.ID 
                |where table_4.NAME = :1 or table_4.NAME = :2""".trimMarginToOneLine(),
            "Alex",
            "Tim"
        ) {
            where(
                or(
                    table
                        .reverseJoinReference(Book::store, JoinType.LEFT)
                        .reverseJoinList(Author::books, JoinType.LEFT)
                        [Author::name] eq "Alex",
                    table.books(JoinType.LEFT).authors(JoinType.LEFT).name eq "Tim"
                )
            )
            select(constant(1))
        }
    }

    @Test
    fun mergeNormalJoinsAndReversedJoinsWithDiffJoinTypes() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as table_1 
                |inner join BOOK as table_2 on table_1.ID = table_2.STORE_ID 
                |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.BOOK_ID 
                |inner join AUTHOR as table_4 on table_3.AUTHOR_ID = table_4.ID 
                |where table_4.NAME = :1 or table_4.NAME = :2""".trimMarginToOneLine(),
            "Alex",
            "Tim"
        ) {
            where(
                or(
                    table
                        .reverseJoinReference(Book::store, JoinType.LEFT)
                        .reverseJoinList(Author::books, JoinType.LEFT)
                        [Author::name] eq "Alex",
                    table.books(JoinType.RIGHT).authors(JoinType.RIGHT).name eq "Tim"
                )
            )
            select(constant(1))
        }
    }
}