package org.babyfish.kimmer.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.babyfish.kimmer.Immutable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = Tiger::class),
    JsonSubTypes.Type(value = Otter::class)
)
interface Animal: Immutable {
    val zoo: Zoo
}