package org.babyfish.kimmer.model

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Otter")
interface Otter: Animal {
    val length: Int
}