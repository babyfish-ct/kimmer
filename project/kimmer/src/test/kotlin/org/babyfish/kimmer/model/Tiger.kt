package org.babyfish.kimmer.model

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Tiger")
interface Tiger: Animal {
    val weight: Int
}