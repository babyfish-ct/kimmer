package org.babyfish.kimmer.sql.query

import org.babyfish.kimmer.sql.ast.between
import org.babyfish.kimmer.sql.common.AbstractQueryTest
import org.babyfish.kimmer.sql.ast.count
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.runtime.dialect.DefaultDialect
import org.babyfish.kimmer.sql.runtime.dialect.MysqlDialect
import org.babyfish.kimmer.sql.runtime.dialect.OracleDialect
import org.babyfish.kimmer.sql.runtime.dialect.SqlServerDialect
import kotlin.test.Test
import java.math.BigDecimal
import kotlin.test.assertFailsWith
import kotlin.test.expect

class PagingTest: AbstractQueryTest() {

    @Test
    fun testCountQuerySkipUnnecessaryJoinOfIgnoredOrderByClause() {

        val query = sqlClient
            .createQuery(Book::class) {
                where(table.price.between(BigDecimal(20), BigDecimal(30)))
                orderBy(table.`store?`.name)
                orderBy(table.name)
                select(table)
            }

        val countQuery = query
            .reselect {
                select(table.id.count())
            }
            .withoutSortingAndPaging()

        countQuery.executeAndExpect {
            sql {
                """select count(tb_1_.ID) from BOOK as tb_1_ 
                    |where tb_1_.PRICE between $1 and $2""".trimMargin()
            }
        }
        query.limit(10, 20).executeAndExpect {
            sql {
                """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                    |from BOOK as tb_1_ 
                    |left join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.PRICE between $1 and $2 
                    |order by tb_2_.NAME asc, tb_1_.NAME asc 
                    |limit $3 offset $4""".trimMargin()
            }
        }
    }

    @Test
    fun testCountQueryKeepNecessaryJoinOfIgnoredOrderByClause() {

        val query = sqlClient
            .createQuery(Book::class) {
                where(table.price.between(BigDecimal(20), BigDecimal(30)))
                orderBy(table.store.name)
                orderBy(table.name)
                select(table)
            }

        val countQuery = query
            .reselect {
                select(table.id.count())
            }
            .withoutSortingAndPaging()

        countQuery.executeAndExpect {
            sql {
                """select count(tb_1_.ID) from BOOK as tb_1_ 
                    |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.PRICE between $1 and $2""".trimMargin()
            }
        }

        query.limit(10, 20).executeAndExpect {
            sql {
                """select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                    |from BOOK as tb_1_ 
                    |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.PRICE between $1 and $2 
                    |order by tb_2_.NAME asc, tb_1_.NAME asc 
                    |limit $3 offset $4""".trimMargin()
            }
        }
    }

    @Test
    fun testCountQuerySkipNecessaryJoinOfIgnoredSelectClause() {

        val query = sqlClient
            .createQuery(Book::class) {
                where(table.price.between(BigDecimal(20), BigDecimal(30)))
                orderBy(table.name)
                select {
                    table then table.`store?`
                }
            }

        val countQuery = query
            .reselect {
                select(table.id.count())
            }
            .withoutSortingAndPaging()

        countQuery.executeAndExpect {
            sql {
                """select count(tb_1_.ID) 
                    |from BOOK as tb_1_ 
                    |where tb_1_.PRICE between $1 and $2"""
                    .trimMargin()
            }
        }

        query.limit(10, 20).executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID, 
                    |tb_1_.STORE_ID, tb_2_.NAME, tb_2_.WEBSITE 
                    |from BOOK as tb_1_ 
                    |left join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.PRICE between $1 and $2 
                    |order by tb_1_.NAME asc 
                    |limit $3 offset $4""".trimMargin()
            }
        }
    }

    @Test
    fun testCountQueryKeepNecessaryJoinOfIgnoredSelectClause() {

        val query = sqlClient
            .createQuery(Book::class) {
                where(table.price.between(BigDecimal(20), BigDecimal(30)))
                orderBy(table.name)
                select {
                    table then table.store
                }
            }

        val countQuery = query
            .reselect {
                select(table.id.count())
            }
            .withoutSortingAndPaging()

        countQuery.executeAndExpect {
            sql {
                """select count(tb_1_.ID) 
                    |from BOOK as tb_1_ 
                    |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.PRICE between $1 and $2"""
                    .trimMargin()
            }
        }

        query.limit(10, 20).executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID, 
                    |tb_1_.STORE_ID, tb_2_.NAME, tb_2_.WEBSITE 
                    |from BOOK as tb_1_ 
                    |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
                    |where tb_1_.PRICE between $1 and $2 
                    |order by tb_1_.NAME asc 
                    |limit $3 offset $4""".trimMargin()
            }
        }
    }

    @Test
    fun testReselectTwice() {
        val query = sqlClient
            .createQuery(Book::class) {
                where(table.price.between(BigDecimal(20), BigDecimal(30)))
                orderBy(table.name)
                select {
                    table then table.store
                }
            }
        assertFailsWith(IllegalStateException::class) {
            query
                .reselect {
                    select(table.id.count())
                }
                .reselect {
                    select(table.id.count())
                }
        }
    }

    @Test
    fun testReselectBaseOnGroupBy() {
        val query = sqlClient
            .createQuery(Book::class) {
                groupBy(table.id)
                select(table.id)
            }
        assertFailsWith(IllegalStateException::class) {
            query
                .reselect {
                    select(table.id.count())
                }
        }
    }

    @Test
    fun testReselectBaseOnAggregation() {
        val query = sqlClient
            .createQuery(Book::class) {
                select(table.id.count())
            }
        assertFailsWith(IllegalStateException::class) {
            query
                .reselect {
                    select(table.id.count())
                }
        }
    }

    @Test
    fun testDefaultDialect() {
        using(DefaultDialect()) {
            sqlClient.createQuery(Book::class) {
                orderBy(table.name)
                select(table.name).distinct().limit(2, 1)
            }.executeAndExpect {
                sql {
                    """select distinct tb_1_.NAME 
                        |from BOOK as tb_1_ 
                        |order by tb_1_.NAME asc 
                        |limit $1 offset $2""".trimMargin()
                }
                variables(2, 1)
                rows {
                    expect(listOf("GraphQL in Action", "Learning GraphQL")) {
                        this
                    }
                }
            }
        }
    }

    @Test
    fun testMySqlDialect() {
        using(MysqlDialect()) {
            sqlClient.createQuery(Book::class) {
                orderBy(table.name)
                select(table.name).distinct().limit(2, 1)
            }.executeAndExpect {
                sql {
                    """select distinct tb_1_.NAME 
                        |from BOOK as tb_1_ 
                        |order by tb_1_.NAME asc 
                        |limit $1, $2""".trimMargin()
                }
                variables(1, 2)
                rows {
                    expect(listOf("GraphQL in Action", "Learning GraphQL")) {
                        this
                    }
                }
            }
        }
    }

    @Test
    fun testSqlServerDialect() {
        using(SqlServerDialect()) {
            sqlClient.createQuery(Book::class) {
                orderBy(table.name)
                select(table.name).distinct().limit(2, 1)
            }.executeAndExpect {
                sql {
                    """select distinct tb_1_.NAME 
                        |from BOOK as tb_1_ 
                        |order by tb_1_.NAME asc 
                        |offset $1 rows fetch next $2 rows only""".trimMargin()
                }
                variables(1, 2)
                rows {
                    expect(listOf("GraphQL in Action", "Learning GraphQL")) {
                        this
                    }
                }
            }
        }
    }

    @Test
    fun testOracleDialect() {

        using(OracleDialect()) {

            // Both limit & offset
            sqlClient.createQuery(Book::class) {
                orderBy(table.name)
                select(table.name).distinct().limit(2, 1)
            }.executeAndExpect {
                sql {
                    """select * from (
                            |select core__.*, rownum rn__ from (
                                |select distinct tb_1_.NAME 
                                |from BOOK as tb_1_ 
                                |order by tb_1_.NAME asc
                            |) core__ where rownum <= $1
                        |) limited__ where rn__ > $2""".trimMargin()
                }
                variables(3, 1)
                rows {
                    expect(listOf("GraphQL in Action", "Learning GraphQL")) {
                        this
                    }
                }
            }

            // Only limit
            sqlClient.createQuery(Book::class) {
                orderBy(table.name)
                select(table.name).distinct().limit(2)
            }.executeAndExpect {
                sql {
                    """select core__.* from (
                            |select distinct tb_1_.NAME 
                            |from BOOK as tb_1_ 
                            |order by tb_1_.NAME asc
                        |) core__ where rownum <= $1""".trimMargin()
                }
                variables(2)
                rows {
                    expect(listOf("Effective TypeScript", "GraphQL in Action")) {
                        this
                    }
                }
            }
        }
    }
}