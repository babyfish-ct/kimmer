package org.babyfish.kimmer.sql.query

import org.babyfish.kimmer.sql.common.AbstractQueryTest
import org.babyfish.kimmer.sql.ast.eq
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Gender
import org.babyfish.kimmer.sql.model.gender
import kotlin.test.Test
import kotlin.test.expect

class ScalarProviderTest: AbstractQueryTest() {

    @Test
    fun test() {
        sqlClient.createQuery(Author::class) {
            where { table.gender eq Gender.MALE }
            select(table)
        }.executeAndExpect {
            sql {
                """select 
                        |tb_1_.ID, 
                        |tb_1_.FIRST_NAME, 
                        |concat(tb_1_.FIRST_NAME, $1, tb_1_.LAST_NAME), 
                        |tb_1_.GENDER, 
                        |tb_1_.LAST_NAME 
                    |from AUTHOR as tb_1_ 
                    |where tb_1_.GENDER = $2""".trimMargin()
            }
            variables(" ", "M")
            rows {
                expect(5) { size }
            }
        }
    }
}