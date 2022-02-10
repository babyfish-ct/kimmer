package org.babyfish.kimmer.sql.ast.model

interface Book: Node {
    val name: String
    val store: BookStore?
    val authors: List<Author>
}