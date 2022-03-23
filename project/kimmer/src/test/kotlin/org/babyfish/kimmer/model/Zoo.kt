package org.babyfish.kimmer.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.babyfish.kimmer.Abstract
import org.babyfish.kimmer.Immutable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = SealedZoo::class),
    JsonSubTypes.Type(value = WildZoo::class)
)
@Abstract
interface Zoo : Immutable {
    val animals: List<Animal>
}