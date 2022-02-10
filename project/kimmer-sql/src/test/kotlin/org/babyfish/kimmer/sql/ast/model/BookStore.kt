package org.babyfish.kimmer.sql.ast.model

interface BookStore: Node {
    val name: String
    val books: List<Book>
}