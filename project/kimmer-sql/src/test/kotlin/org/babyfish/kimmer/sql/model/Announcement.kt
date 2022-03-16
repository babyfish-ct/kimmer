package org.babyfish.kimmer.sql.model

import org.babyfish.kimmer.sql.Entity

interface Announcement: Entity<Int> {
    val message: String
    val store: BookStore
}