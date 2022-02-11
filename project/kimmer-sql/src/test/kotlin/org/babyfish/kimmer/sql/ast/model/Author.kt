package org.babyfish.kimmer.sql.ast.model

import org.babyfish.kimmer.sql.Entity

interface Author: Entity<String> {
    val name: String
    val books: List<Book>
}