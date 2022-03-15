package org.babyfish.kimmer.sql.model

import org.babyfish.kimmer.sql.Entity
import java.util.*

interface Author: Entity<UUID> {
    val firstName: String
    val lastName: String
    val fullName: String
    val gender: Gender
    val books: List<Book>
}