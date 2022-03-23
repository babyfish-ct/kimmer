package org.babyfish.kimmer.model

import java.math.BigDecimal

interface Book: Node {
    val name: String
    val price: BigDecimal
    val store: BookStore?
    val authors: List<Author>
}