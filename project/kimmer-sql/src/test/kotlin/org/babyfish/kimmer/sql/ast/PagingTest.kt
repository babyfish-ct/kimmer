package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.*
import kotlin.test.Test
import java.math.BigDecimal
import kotlin.test.expect

class PagingTest: AbstractTest() {

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
            .creator {
                select(table.id.count())
            }.create {
                withoutSortingAndPaging()
            }

        execute(countQuery)
        expect("""select count(tb_1_.ID) from BOOK as tb_1_ 
            |where tb_1_.PRICE between $1 and $2""".trimMargin().toOneLine()
        ) { sql }

        execute(query.create {
            limit(10, 20)
        })
        expect("""select tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
            |from BOOK as tb_1_ 
            |left join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
            |where tb_1_.PRICE between $1 and $2 
            |order by tb_2_.NAME asc, tb_1_.NAME asc 
            |limit $3 offset $4""".trimMargin().toOneLine()
        ) { sql }
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
            .creator {
                select(table.id.count())
            }.create {
                withoutSortingAndPaging()
            }

        execute(countQuery)
        expect("""select count(tb_1_.ID) from BOOK as tb_1_ 
            |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
            |where tb_1_.PRICE between $1 and $2""".trimMargin().toOneLine()
        ) { sql }

        execute(query.create {
            limit(10, 20)
        })
        expect("""select tb_1_.EDITION, tb_1_.ID, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
            |from BOOK as tb_1_ 
            |inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
            |where tb_1_.PRICE between $1 and $2 
            |order by tb_2_.NAME asc, tb_1_.NAME asc 
            |limit $3 offset $4""".trimMargin().toOneLine()
        ) { sql }
    }
}