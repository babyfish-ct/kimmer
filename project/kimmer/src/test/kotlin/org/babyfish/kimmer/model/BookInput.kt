package org.babyfish.kimmer.model

import org.babyfish.kimmer.graphql.Input
import java.util.*

interface BookInput: Input {
    val id: Long
    val name: String
    val storeId: UUID
    val bookIds: List<Long>
}