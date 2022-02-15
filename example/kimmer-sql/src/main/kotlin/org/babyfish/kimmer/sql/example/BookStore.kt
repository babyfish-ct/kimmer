package org.babyfish.kimmer.sql.example

import org.babyfish.kimmer.sql.Entity
import java.util.*

interface BookStore: Entity<UUID> {
    val name: String
    val books: List<Book>
}