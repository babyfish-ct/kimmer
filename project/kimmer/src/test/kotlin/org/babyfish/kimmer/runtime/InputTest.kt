package org.babyfish.kimmer.runtime

import org.babyfish.kimmer.model.BookInput
import org.babyfish.kimmer.model.by
import org.babyfish.kimmer.new
import org.junit.Ignore
import org.junit.Test
import kotlin.test.expect

class InputTest {

    @Ignore
    @Test
    fun test() {
        val input = new(BookInput::class).by {
            id = 1L
            name = "Learning GraphQL"
            bookIds() += 1L
            bookIds() += 2L
            bookIds() += 3L
            bookIds() += 4L
        }
        val json = input.toString()
        expect("") { json }
    }
}