package org.babyfish.kimmer.model

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("SealedZoo")
interface SealedZoo: Zoo {
    val location: String
}