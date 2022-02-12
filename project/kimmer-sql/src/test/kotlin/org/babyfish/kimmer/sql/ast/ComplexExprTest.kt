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
                    |table_1.ID, 
                    |table_1.NAME, 
                    |table_1.PRICE, 
                    |table_1.STORE_ID, 
                    |rank() over(
                       |order by table_1.PRICE asc
                    |), 
                    |rank() over(
                        |partition by table_1.STORE_ID 
                        |order by table_1.PRICE asc
                    |) 
                |from BOOK as table_1""".trimMargin().toOneLine()
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