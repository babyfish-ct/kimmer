package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test
import java.math.BigDecimal

class ComplexExprTest: AbstractTest() {

    @Test
    fun testSqlExpression() {
        testQuery(
            Book::class,
            """select 
                    |tb_1_.EDITION, 
                    |tb_1_.ID, 
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
                |from BOOK as tb_1_""".trimMargin().toOneLine()
        ) {
            select(
                table,
                sql(BigDecimal::class, "rank() over(order by %e desc)") {
                    expressions(table.price)
                },
                sql(BigDecimal::class, "rank() over(partition by %e order by %e desc)") {
                    expressions(table.store.id, table.price)
                }
            )
        }
    }

    @Test
    fun testTupleInList() {
        testQuery(
            Book::class,
            """select 
                |tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
            |from BOOK as tb_1_ 
            |where (tb_1_.NAME, tb_1_.EDITION) in (
                |($1, $2), ($3, $4)
            |)""".trimMargin().toOneLine(),
            "Learning GraphQL",
            3,
            "Effective TypeScript",
            2
        ) {
            where(
                tuple(table.name, table.edition) valueIn listOf(
                    "Learning GraphQL" to 3,
                    "Effective TypeScript" to 2
                )
            )
            select(table)
        }
    }

    @Test
    fun testSimpleCase() {
        testQuery(
            BookStore::class,
            """select 
                |tb_1_.ID, 
                |tb_1_.NAME, 
                |tb_1_.WEBSITE, 
                |case tb_1_.NAME 
                    |when $1 then $2 
                    |when $3 then $4 
                    |else $5 
                |end 
                |from BOOK_STORE as tb_1_""".trimMargin().toOneLine(),
            "O'RELLIY",
            "Classic publishing house",
            "MANNING",
            "Classic publishing house",
            "Other publishing house"
        ) {
            select(
                table,
                case(table.name)
                    .match("O'RELLIY", "Classic publishing house")
                    .match("MANNING", "Classic publishing house")
                    .otherwise("Other publishing house")
            )
        }
    }

    @Test
    fun testCase() {
        testQuery(
            Book::class,
            """select 
                |tb_1_.EDITION, 
                |tb_1_.ID, 
                |tb_1_.NAME, 
                |tb_1_.PRICE, 
                |tb_1_.STORE_ID, 
                |case 
                    |when tb_1_.PRICE > $1 then $2 
                    |when tb_1_.PRICE < $3 then $4 
                    |else $5 
                |end 
                |from BOOK as tb_1_""".trimMargin().toOneLine(),
            BigDecimal(200),
            "Expensive",
            BigDecimal(100),
            "Cheap",
            "Fitting"
        ) {
            select(
                table,
                case()
                    .match(table.price gt BigDecimal(200), "Expensive")
                    .match(table.price lt BigDecimal(100), "Cheap")
                    .otherwise("Fitting")
            )
        }
    }
}