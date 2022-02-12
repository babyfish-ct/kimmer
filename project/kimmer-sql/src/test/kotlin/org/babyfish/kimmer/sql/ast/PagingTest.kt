package org.babyfish.kimmer.sql.ast

import kotlinx.coroutines.selects.select
import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.name
import org.babyfish.kimmer.sql.ast.model.price
import org.babyfish.kimmer.sql.ast.model.store
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.expect

class PagingTest: AbstractTest() {

    @Test
    fun testPaginateQuery() {

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

        expect("""select count(table_1.ID) 
            |from BOOK as table_1 where table_1.PRICE between :1 and :2""".trimMargin().toOneLine()
        ) { sql }
        execute(countQuery)

        execute(query.create {
            limit(10, 20)
        })
        expect("""select table_1.ID, table_1.NAME, table_1.PRICE, table_1.STORE_ID 
            |from BOOK as table_1 
            |inner join BOOK_STORE as table_2 on table_1.STORE_ID = table_2.ID 
            |where table_1.PRICE between :1 and :2 
            |order by table_2.NAME asc, table_1.NAME asc 
            |limit :3 offset :4""".trimMargin().toOneLine()) { sql }
    }
}