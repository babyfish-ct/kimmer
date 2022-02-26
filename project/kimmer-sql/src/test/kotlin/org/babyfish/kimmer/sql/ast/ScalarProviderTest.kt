package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.ast.common.AbstractTest
import org.babyfish.kimmer.sql.ast.model.Author
import org.babyfish.kimmer.sql.ast.model.Gender
import org.babyfish.kimmer.sql.ast.model.gender
import kotlin.test.Test
import kotlin.test.expect

class ScalarProviderTest: AbstractTest() {

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