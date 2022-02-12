package org.babyfish.kimmer.sql.ast.model

import org.babyfish.kimmer.sql.Entity
import java.math.BigDecimal

interface Book: Entity<String> {
    val name: String
    val price: BigDecimal
    val edition: Int
    val store: BookStore?
    val authors: List<Author>
}
