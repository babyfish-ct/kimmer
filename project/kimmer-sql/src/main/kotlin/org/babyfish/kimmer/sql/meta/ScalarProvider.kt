package org.babyfish.kimmer.sql.meta

import kotlin.reflect.KClass

interface ScalarProvider<T: Any, S: Any> {
    val scalarType: KClass<T>
    val sqlType: KClass<S>
    fun toScalar(sqlValue: S): T
    fun toSql(scalarValue: T): S
}

fun <E: Enum<E>> enumProviderByString(
    enumType: KClass<E>,
    block: (EnumProviderBuilder<E, String>.() -> Unit)? = null
): ScalarProvider<E, String> =
    EnumProviderBuilder(enumType, String::class) {
        it.name
    }.apply {
        if (block !== null) {
            block()
        }
    }.build()

fun <E: Enum<E>> enumProviderByInt(
    enumType: KClass<E>,
    block: (EnumProviderBuilder<E, Int>.() -> Unit)? = null
): ScalarProvider<E, Int> =
    EnumProviderBuilder(enumType, Int::class) {
        it.ordinal
    }.apply {
        if (block !== null) {
            block()
        }
    }.build()

private class EnumProvider<E: Enum<E>, S: Any> internal constructor(
    override val scalarType: KClass<E>,
    override val sqlType: KClass<S>,
    private val enumMap: Map<S, E>,
    private val sqlMap: Map<E, S>
): ScalarProvider<E, S> {

    override fun toScalar(sqlValue: S): E =
        enumMap[sqlValue]
            ?: throw IllegalArgumentException(
                "Cannot resolve '$scalarType' by the value '${sqlValue}'"
            )

    override fun toSql(enumValue: E): S =
        sqlMap[enumValue]
            ?: error("Internal bug: Enum can be converted to sql value absolutely")
}

class EnumProviderBuilder<E: Enum<E>, S: Any> internal constructor(
    private val scalarType: KClass<E>,
    private val sqlType: KClass<S>,
    private val defaultSqlValue: (E) -> S
) {

    private val sqlMap = mutableMapOf<E, S>()

    fun map(enumValue: E, sqlValue: S) {
        if (sqlMap.contains(enumValue)) {
            error("'${enumValue}' has been mapped")
        }
        sqlMap[enumValue] = sqlValue
    }

    internal fun build(): ScalarProvider<E, S> {
        val sqlMap = sqlMap.toMutableMap()
        for (enumValue in scalarType.java.enumConstants) {
            sqlMap.computeIfAbsent(enumValue, defaultSqlValue)
        }
        val enumMap = mutableMapOf<S, E>()
        for (e in sqlMap.entries) {
            val conflictEnum = enumMap.put(e.value, e.key)
            if (conflictEnum !== null) {
                error("Both '${e.key}' and '${conflictEnum}' are mapped as '${e.value}'")
            }
        }
        return EnumProvider(
            scalarType,
            sqlType,
            enumMap,
            sqlMap
        )
    }
}
