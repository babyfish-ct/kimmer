package org.babyfish.kimmer.model

interface Author: Node {
    val name: String
    val books: List<Book>
}