package org.babyfish.kimmer.sql.model

import org.babyfish.kimmer.sql.Entity

interface Chapter: Entity<Long> {
    val name: String
    val book: Book
}