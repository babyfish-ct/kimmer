package org.babyfish.kimmer.example.model

import org.babyfish.kimmer.Immutable

interface Book: Immutable {
    val name: String
    val store: BookStore?
    val authors: List<Author>
}