package org.babyfish.kimmer.model

import org.babyfish.kimmer.sql.Entity

interface Consumer: Entity<String> {
    val name: String
}