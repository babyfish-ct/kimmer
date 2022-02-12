package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.price
import org.babyfish.kimmer.sql.ast.model.store
import org.junit.Test
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
                       |order by tb_1_.PRICE asc
                    |), 
                    |rank() over(
                        |partition by tb_1_.STORE_ID 
                        |order by tb_1_.PRICE asc
                    |) 
                |from BOOK as tb_1_""".trimMargin().toOneLine()
        ) {
            select(
                table,
                sql(BigDecimal::class, "rank() over(order by %e asc)") {
                    expressions(table.price)
                },
                sql(BigDecimal::class, "rank() over(partition by %e order by %e asc)") {
                    expressions(table.store.id, table.price)
                }
            )
        }
    }
}