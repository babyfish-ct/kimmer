package org.babyfish.kimmer.sql.runtime

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

internal fun convert(value: Any, expectedType: KClass<*>): Any? =
    convert(value, expectedType.java)

internal fun convert(value: Any, expectedType: Class<*>): Any? {
    if (value::class.java === expectedType) {
        return value
    }
    if (value is Number) {
        return when (expectedType) {
            Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType ->
                value.toInt() != 0
            Byte::class.javaObjectType, Byte::class.javaPrimitiveType ->
                value.toByte()
            Short::class.javaObjectType, Short::class.javaPrimitiveType ->
                value.toShort()
            Int::class.javaObjectType, Int::class.javaPrimitiveType ->
                value.toInt()
            Long::class.javaObjectType, Long::class.javaPrimitiveType ->
                value.toLong()
            Float::class.javaObjectType, Float::class.javaPrimitiveType ->
                value.toFloat()
            Double::class.javaObjectType, Double::class.javaPrimitiveType ->
                value.toDouble()
            BigInteger::class.java ->
                when (value) {
                    is BigDecimal -> value.toBigInteger()
                    else -> BigInteger.valueOf(value.toLong())
                }
            BigDecimal::class.java ->
                when (value) {
                    is BigInteger -> value.toBigDecimal()
                    is Float, is Double -> BigDecimal.valueOf(value.toLong())
                    else -> BigDecimal.valueOf(value.toDouble())
                }
            else ->
                null
        }
    }
    return null
}