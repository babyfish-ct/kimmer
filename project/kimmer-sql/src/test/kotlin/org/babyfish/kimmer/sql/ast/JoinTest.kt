package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.*
import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test
import java.math.BigDecimal
import java.util.*

class JoinTest : AbstractTest() {

    @Test
    fun testSimple() {
        testQuery(
            Book::class,
            "select tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID from BOOK as tb_1_"
        ) {
            select(table)
        }
    }

    @Test
    fun testMergedJoinFromParentToChild() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as tb_1_ 
            |inner join BOOK as tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
            |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.BOOK_ID 
            |inner join AUTHOR as tb_4_ on tb_3_.AUTHOR_ID = tb_4_.ID 
            |where tb_2_.PRICE >= $1 
            |and tb_2_.PRICE <= $2 
            |and lower(tb_4_.FIRST_NAME) like $3""".trimMargin().toOneLine(),
            BigDecimal(20),
            BigDecimal(30),
            "alex"
        ) {
            where(table.books(JoinType.LEFT).price ge BigDecimal(20))
            where(table.books(JoinType.RIGHT).price le BigDecimal(30))
            where(table.books.authors.firstName ilike "Alex")
            select(constant(1))
        }
    }

    @Test
    fun testMergedJoinFromChildToParent() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as tb_1_ 
            |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
            |inner join BOOK as tb_3_ on tb_2_.BOOK_ID = tb_3_.ID 
            |inner join BOOK_STORE as tb_4_ on tb_3_.STORE_ID = tb_4_.ID 
            |where tb_3_.PRICE <= $1 
            |and tb_3_.PRICE <= $2 
            |and lower(tb_4_.NAME) like $3""".trimMargin().toOneLine(),
            BigDecimal(20),
            BigDecimal(30),
            "manning"
        ) {
            where(table.books(JoinType.LEFT).price le BigDecimal(20))
            where(table.books(JoinType.RIGHT).price le BigDecimal(30))
            where(table.books.store.name ilike "MANNING")
            select(constant(1))
        }
    }

    @Test
    fun testUnnecessaryJoin() {
        testQuery(
            Book::class,
            "select 1 from BOOK as tb_1_ where tb_1_.STORE_ID in ($1, $2)",
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(
                table.store.id valueIn listOf(learningGraphQLId1, learningGraphQLId2)
            )
            select(constant(1))
        }
    }

    @Test
    fun testHalfJoin() {
        testQuery(
            Book::class,
            """select 1 from BOOK as tb_1_ 
            |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.BOOK_ID 
            |where tb_2_.AUTHOR_ID in ($1, $2)""".trimMargin().toOneLine(),
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(table.authors.id valueIn listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }
    }

    @Test
    fun testHalfInverseJoin() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as tb_1_ 
            |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
            |where tb_2_.BOOK_ID in ($1, $2)""".trimMargin().toOneLine(),
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(table.books.id valueIn listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }
    }

    @Test
    fun testOneToManyCannotBeOptimized() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as tb_1_ 
            |inner join BOOK as tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
            |where tb_2_.ID in ($1, $2)""".trimMargin().toOneLine(),
            learningGraphQLId1,
            learningGraphQLId2
        ) {
            where(table.books.id valueIn listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }
    }

    @Test
    fun testOuterJoin() {
        testQuery(
            Book::class,
            """select 1 from BOOK as tb_1_ 
            |left join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
            |where tb_1_.STORE_ID is null 
            |or lower(tb_2_.NAME) like $1""".trimMargin().toOneLine(),
            "manning"
        ) {
            where(
                or(
                    table.store(JoinType.LEFT).id.isNull(),
                    table.store(JoinType.LEFT).name ilike "MANNING"
                )
            )
            select(constant(1))
        }
    }
}
