package org.babyfish.kimmer.sql.query

import org.babyfish.kimmer.sql.ast.eq
import org.babyfish.kimmer.sql.ast.table.source
import org.babyfish.kimmer.sql.ast.table.target
import org.babyfish.kimmer.sql.common.*
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.firstName
import org.babyfish.kimmer.sql.model.name
import org.junit.Test
import kotlin.test.expect

class AssociationQueryTest : AbstractQueryTest() {

    @Test
    fun test() {
        sqlClient.queries.byList(Book::authors) {
            where(
                table.source.name eq "Learning GraphQL",
                table.target.firstName eq "Alex"
            )
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                        |tb_1_.BOOK_ID, tb_1_.AUTHOR_ID 
                    |from BOOK_AUTHOR_MAPPING as tb_1_ 
                    |inner join BOOK as tb_2_ on tb_1_.BOOK_ID = tb_2_.ID 
                    |inner join AUTHOR as tb_3_ on tb_1_.AUTHOR_ID = tb_3_.ID 
                    |where 
                        |tb_2_.NAME = $1 
                    |and 
                        |tb_3_.FIRST_NAME = $2""".trimMargin()
            }
            variables("Learning GraphQL", "Alex")
            rows {
                expect(
                    setOf(
                        learningGraphQLId1 to alexId,
                        learningGraphQLId2 to alexId,
                        learningGraphQLId3 to alexId
                    )
                ) {
                    map {
                        it.source.id to it.target.id
                    }.toSet()
                }
            }
        }
    }

    @Test
    fun inverseTest() {
        sqlClient.queries.byList(Author::books) {
            where(
                table.source.firstName eq "Alex",
                table.target.name eq "Learning GraphQL"
            )
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                        |tb_1_.AUTHOR_ID, tb_1_.BOOK_ID 
                    |from BOOK_AUTHOR_MAPPING as tb_1_ 
                    |inner join AUTHOR as tb_2_ on tb_1_.AUTHOR_ID = tb_2_.ID 
                    |inner join BOOK as tb_3_ on tb_1_.BOOK_ID = tb_3_.ID 
                    |where 
                        |tb_2_.FIRST_NAME = $1 
                    |and 
                        |tb_3_.NAME = $2""".trimMargin()
            }
            variables("Alex", "Learning GraphQL")
            rows {
                expect(
                    setOf(
                        alexId to learningGraphQLId1,
                        alexId to learningGraphQLId2,
                        alexId to learningGraphQLId3,
                    )
                ) {
                    map {
                        it.source.id to it.target.id
                    }.toSet()
                }
            }
        }
    }
}