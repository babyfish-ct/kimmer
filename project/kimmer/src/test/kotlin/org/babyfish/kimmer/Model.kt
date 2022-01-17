package org.babyfish.kimmer

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal

@Abstract
interface Node: Immutable {
    val id: String
}

interface BookStore: Node {
    val name: String
    val books: List<Book>
    val avgPrice: BigDecimal
}

interface Book: Node {
    val name: String
    val price: BigDecimal
    val store: BookStore?
    val authors: List<Author>
}

interface Author: Node {
    val name: String
    val books: List<Book>
}

//////////////////////////////////

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = SealedZoo::class),
    JsonSubTypes.Type(value = WildZoo::class)
)
@Abstract
interface Zoo : Immutable {
    val animals: List<Animal>
}

@JsonTypeName("SealedZoo")
interface SealedZoo: Zoo {
    val location: String
}

@JsonTypeName("WildZoo")
interface WildZoo: Zoo {
    val area: Long
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = Tiger::class),
    JsonSubTypes.Type(value = Otter::class)
)
interface Animal: Immutable {
    val zoo: Zoo
}

@JsonTypeName("Tiger")
interface Tiger: Animal {
    val weight: Int
}

@JsonTypeName("Otter")
interface Otter: Animal {
    val length: Int
}

//////////////////////////////////

interface PrimitiveInfo : Immutable {
    val boolean: Boolean
    val char: Char
    val byte: Byte
    val short: Short
    val int: Int
    val long: Long
    val float: Float
    val double: Double
}

//////////////////////////////////

interface Employee: Immutable {
    val supervisor: Employee?
}