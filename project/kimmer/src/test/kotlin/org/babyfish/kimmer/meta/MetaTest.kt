package org.babyfish.kimmer.meta

import org.babyfish.kimmer.Book
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.test.Test

class MetaTest {

    @Test
    fun test() {
        ImmutableType.of(Book::class)
    }
}