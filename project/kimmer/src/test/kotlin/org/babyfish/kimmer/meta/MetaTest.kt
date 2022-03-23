package org.babyfish.kimmer.meta

import org.babyfish.kimmer.model.Book
import kotlin.test.Test

class MetaTest {

    @Test
    fun test() {
        ImmutableType.of(Book::class)
    }
}