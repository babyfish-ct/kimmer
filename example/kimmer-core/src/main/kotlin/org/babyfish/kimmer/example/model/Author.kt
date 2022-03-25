package org.babyfish.kimmer.example.model

import org.babyfish.kimmer.Immutable

interface Author: Immutable {
    val name: String
    val books: List<Book>
}