package org.babyfish.kimmer.sql.example

import org.babyfish.kimmer.sql.Entity
import java.util.*

interface Author: Entity<UUID> {
    val name: String
    val books: List<Book>
}