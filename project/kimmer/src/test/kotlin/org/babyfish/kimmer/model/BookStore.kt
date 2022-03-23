package org.babyfish.kimmer.model

import java.math.BigDecimal

interface BookStore: Node {
    val name: String
    val books: List<Book>
    val avgPrice: BigDecimal
}
