package org.babyfish.kimmer.example.model

import org.babyfish.kimmer.Immutable

interface BookStore: Immutable {
    val name: String
    val books: List<Book>
}