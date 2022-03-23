package org.babyfish.kimmer.model

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("WildZoo")
interface WildZoo: Zoo {
    val area: Long
}