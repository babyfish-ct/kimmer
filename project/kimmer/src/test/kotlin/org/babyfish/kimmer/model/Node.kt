package org.babyfish.kimmer.model

import org.babyfish.kimmer.Abstract
import org.babyfish.kimmer.Immutable

@Abstract
interface Node: Immutable {
    val id: String
}