package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.model.*
import org.junit.Test
import java.math.BigDecimal

class JoinTest: AbstractTest() {

    @Test
    fun testSimple() {
        testQuery(
            Book::class,
            "select table_1.NAME from BOOK as table_1"
        ) {
            select(table.name)
        }
    }

    @Test
    fun testMergedJoinFromParentToChild() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as table_1 
            |inner join BOOK as table_2 on table_1.ID = table_2.STORE_ID 
            |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.BOOK_ID 
            |inner join AUTHOR as table_4 on table_3.AUTHOR_ID = table_4.ID 
            |where table_2.PRICE >= :1 
            |and table_2.PRICE <= :2 
            |and lower(table_4.NAME) like :3""".trimMarginToOneLine(),
            BigDecimal(20),
            BigDecimal(30),
            "alex"
        ) {
            where(table.books(JoinType.LEFT).price ge BigDecimal(20))
            where(table.books(JoinType.RIGHT).price le BigDecimal(30))
            where(table.books.authors.name ilike "Alex")
            select(constant(1))
        }
    }

    @Test
    fun testMergedJoinFromChildToParent() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as table_1 
            |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.AUTHOR_ID 
            |inner join BOOK as table_3 on table_2.BOOK_ID = table_3.ID 
            |inner join BOOK_STORE as table_4 on table_3.STORE_ID = table_4.ID 
            |where table_3.PRICE <= :1 
            |and table_3.PRICE <= :2 
            |and lower(table_4.NAME) like :3""".trimMarginToOneLine(),
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
            "select 1 from BOOK as table_1 where table_1.STORE_ID in (:1, :2)",
            "id1",
            "id2"
        ) {
            where(table.store.id valueIn listOf("id1", "id2"))
            select(constant(1))
        }
    }

    @Test
    fun testHalfJoin() {
        testQuery(
            Book::class,
            """select 1 from BOOK as table_1 
            |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.BOOK_ID 
            |where table_2.AUTHOR_ID in (:1, :2)""".trimMarginToOneLine(),
            "id1",
            "id2"
        ) {
            where(table.authors.id valueIn listOf("id1", "id2"))
            select(constant(1))
        }
    }

    @Test
    fun testHalfInverseJoin() {
        testQuery(
            Author::class,
            """select 1 from AUTHOR as table_1 
            |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.AUTHOR_ID 
            |where table_2.BOOK_ID in (:1, :2)""".trimMarginToOneLine(),
            "id1",
            "id2"
        ) {
            where(table.books.id valueIn listOf("id1", "id2"))
            select(constant(1))
        }
    }

    @Test
    fun testOneToManyCannotBeOptimized() {
        testQuery(
            BookStore::class,
            """select 1 from BOOK_STORE as table_1 
            |inner join BOOK as table_2 on table_1.ID = table_2.STORE_ID 
            |where table_2.ID in (:1, :2)""".trimMarginToOneLine(),
            "id1",
            "id2"
        ) {
            where(table.books.id valueIn listOf("id1", "id2"))
            select(constant(1))
        }
    }

    @Test
    fun testOuterJoin() {
        testQuery(
            Book::class,
            """select 1 from BOOK as table_1 
            |left join BOOK_STORE as table_2 on table_1.STORE_ID = table_2.ID 
            |where table_1.STORE_ID is null 
            |or lower(table_2.NAME) like :1""".trimMarginToOneLine(),
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
