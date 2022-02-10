package org.babyfish.kimmer.sql.ast.model

interface Author: Node {
    val name: String
    val books: List<Book>
}