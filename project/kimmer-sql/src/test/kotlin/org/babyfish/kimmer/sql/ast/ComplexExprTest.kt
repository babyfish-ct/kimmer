package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test
import java.math.BigDecimal
import kotlin.test.expect

class ComplexExprTest: AbstractTest() {

    @Test
    fun testSqlExpression() {
        sqlClient.createQuery(Book::class) {
            select {
                table then
                sql(Int::class, "rank() over(order by %e desc)") {
                    expressions(table.price)
                } then
                sql(Int::class, "rank() over(partition by %e order by %e desc)") {
                    expressions(table.store.id, table.price)
                }
            }
        }.executeAndExpect {
            sql {
                """select 
                    |tb_1_.ID, 
                    |tb_1_.EDITION, 
                    |tb_1_.NAME, 
                    |tb_1_.PRICE, 
                    |tb_1_.STORE_ID, 
                    |rank() over(
                        |order by tb_1_.PRICE desc
                    |), 
                    |rank() over(
                        |partition by tb_1_.STORE_ID 
                        |order by tb_1_.PRICE desc
                    |) 
                |from BOOK as tb_1_""".trimMargin()
            }
            variables()
            rows {
                expect(12) { size }
                val rank1 = first { it.first.price.toInt() == 88 }
                val rank12 = first { it.first.price.toInt() == 45 }
                expect(1) { rank1.second }
                expect(1) { rank1.third }
                expect(12) { rank12.second }
                expect(9) { rank12.third }
            }
        }
    }

    @Test
    fun testTupleInList() {
        sqlClient.createQuery(Book::class) {
            where(
                tuple {
                    table.name then table.edition
                } valueIn listOf(
                    "Learning GraphQL" to 3,
                    "Effective TypeScript" to 2
                )
            )
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                |tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
                    |from BOOK as tb_1_ 
                    |where (tb_1_.NAME, tb_1_.EDITION) in (
                    |($1, $2), ($3, $4)
                |)""".trimMargin()
            }
            variables("Learning GraphQL", 3, "Effective TypeScript", 2)
        }
    }

    @Test
    fun testSimpleCase() {
        sqlClient.createQuery(BookStore::class) {
            select {
                table then
                    case(table.name)
                        .match("O'RELLIY", "Classic publishing house")
                        .match("MANNING", "Classic publishing house")
                        .otherwise("Other publishing house")
            }
        }.executeAndExpect {
            sql {
                """select 
                |tb_1_.ID, 
                |tb_1_.NAME, 
                |tb_1_.WEBSITE, 
                |case tb_1_.NAME 
                    |when $1 then $2 
                    |when $3 then $4 
                    |else $5 
                |end 
                |from BOOK_STORE as tb_1_""".trimMargin()
            }
            variables(
                "O'RELLIY",
                "Classic publishing house",
                "MANNING",
                "Classic publishing house",
                "Other publishing house"
            )
        }
    }

    @Test
    fun testCase() {
        sqlClient.createQuery(Book::class) {
            select {
                table then
                case()
                    .match(table.price gt BigDecimal(200), "Expensive")
                    .match(table.price lt BigDecimal(100), "Cheap")
                    .otherwise("Fitting")
            }
        }.executeAndExpect {
            sql {
                """select 
                |tb_1_.ID, 
                |tb_1_.EDITION, 
                |tb_1_.NAME, 
                |tb_1_.PRICE, 
                |tb_1_.STORE_ID, 
                |case 
                    |when tb_1_.PRICE > $1 then $2 
                    |when tb_1_.PRICE < $3 then $4 
                    |else $5 
                |end 
                |from BOOK as tb_1_""".trimMargin()
            }
            variables(
                BigDecimal(200),
                "Expensive",
                BigDecimal(100),
                "Cheap",
                "Fitting"
            )
        }
    }
}