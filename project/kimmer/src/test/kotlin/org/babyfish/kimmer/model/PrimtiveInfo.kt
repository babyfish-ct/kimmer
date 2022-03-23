package org.babyfish.kimmer.model

import org.babyfish.kimmer.Immutable

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