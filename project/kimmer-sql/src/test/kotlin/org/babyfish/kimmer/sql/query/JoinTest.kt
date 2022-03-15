package org.babyfish.kimmer.sql.query

import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.common.AbstractQueryTest
import org.babyfish.kimmer.sql.common.learningGraphQLId1
import org.babyfish.kimmer.sql.common.learningGraphQLId2
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.BookStore
import kotlin.test.Test
import java.math.BigDecimal

class JoinTest : AbstractQueryTest() {

    @Test
    fun testSimple() {
        sqlClient.createQuery(Book::class) {
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, 
                    |tb_1_.EDITION, 
                    |tb_1_.NAME, 
                    |tb_1_.PRICE, 
                    |tb_1_.STORE_ID 
                    |from BOOK as tb_1_""".trimMargin()
            }
            variables()
        }
    }

    @Test
    fun testMergedJoinFromParentToChild() {
        sqlClient.createQuery(BookStore::class) {
            where(table.`books?`.price ge BigDecimal(20))
            where(table.books.price le BigDecimal(30))
            where(table.books.authors.firstName ilike "Alex")
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                    |inner join BOOK as tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
                    |inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.BOOK_ID 
                    |inner join AUTHOR as tb_4_ on tb_3_.AUTHOR_ID = tb_4_.ID 
                    |where tb_2_.PRICE >= $1 
                    |and tb_2_.PRICE <= $2 
                    |and lower(tb_4_.FIRST_NAME) like $3""".trimMargin()
            }
            variables(BigDecimal(20), BigDecimal(30), "%alex%")
        }
    }

    @Test
    fun testMergedJoinFromChildToParent() {
        sqlClient.createQuery(Author::class) {
            where(table.`books?`.price le BigDecimal(20))
            where(table.books.price le BigDecimal(30))
            where(table.books.store.name ilike "MANNING")
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from AUTHOR as tb_1_ 
                    |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.AUTHOR_ID 
                    |inner join BOOK as tb_3_ on tb_2_.BOOK_ID = tb_3_.ID 
                    |inner join BOOK_STORE as tb_4_ on tb_3_.STORE_ID = tb_4_.ID 
                    |where tb_3_.PRICE <= $1 
                    |and tb_3_.PRICE <= $2 
                    |and lower(tb_4_.NAME) like $3""".trimMargin()
            }
            variables(BigDecimal(20), BigDecimal(30), "%manning%")
        }
    }

    @Test
    fun testUnnecessaryJoin() {
        sqlClient.createQuery(Book::class) {
            where(
                table.store.id valueIn listOf(learningGraphQLId1, learningGraphQLId2)
            )
            select(constant(1))
        }.executeAndExpect {
            sql {
                "select 1 from BOOK as tb_1_ where tb_1_.STORE_ID in ($1, $2)"
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun testHalfJoin() {
        sqlClient.createQuery(Book::class) {
            where(table.authors.id valueIn listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                |inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.BOOK_ID 
                |where tb_2_.AUTHOR_ID in ($1, $2)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun testHalfInverseJoin() {
        sqlClient.createQuery(Author::class) {
            where(table.books.id valueIn listOf(learningGraphQLId1, learningGraphQLId2))
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
    fun testOneToManyCannotBeOptimized() {
        sqlClient.createQuery(BookStore::class) {
            where(table.books.id valueIn listOf(learningGraphQLId1, learningGraphQLId2))
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK_STORE as tb_1_ 
                |inner join BOOK as tb_2_ on tb_1_.ID = tb_2_.STORE_ID 
                |where tb_2_.ID in ($1, $2)""".trimMargin()
            }
            variables(learningGraphQLId1, learningGraphQLId2)
        }
    }

    @Test
    fun testOuterJoin() {
        sqlClient.createQuery(Book::class) {
            where(
                or(
                    table.`store?`.id.isNull(),
                    table.`store?`.name ilike "MANNING"
                )
            )
            select(constant(1))
        }.executeAndExpect {
            sql {
                """select 1 from BOOK as tb_1_ 
                    |left join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.STORE_ID is null 
                    |or lower(tb_2_.NAME) like $1""".trimMargin()
            }
            variables("%manning%")
        }
    }
}
