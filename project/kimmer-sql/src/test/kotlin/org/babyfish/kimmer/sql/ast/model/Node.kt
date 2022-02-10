package org.babyfish.kimmer.sql.ast.model

import org.babyfish.kimmer.Immutable

interface Node: Immutable {
    val id: String
}