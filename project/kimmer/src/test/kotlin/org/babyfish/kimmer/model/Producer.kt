package org.babyfish.kimmer.model

import org.babyfish.kimmer.sql.Entity

interface Producer: Entity<String> {
    val name: String
}