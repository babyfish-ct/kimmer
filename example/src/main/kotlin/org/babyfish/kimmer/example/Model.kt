package org.babyfish.kimmer.example

import org.babyfish.kimmer.Immutable

interface BookStore: Immutable {
    val name: String
    val books: List<Book>
}

interface Book: Immutable {
    val name: String
    val store: BookStore
    val authors: List<Author>
}

interface Author: Immutable {
    val name: String
    val books: List<Book>
}