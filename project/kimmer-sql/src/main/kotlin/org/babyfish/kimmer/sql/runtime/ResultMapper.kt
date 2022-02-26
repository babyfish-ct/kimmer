package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Row
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.ResultSet
import kotlin.reflect.KClass

internal abstract class ResultMapper(
    private val sqlClient: SqlClient,
    baseIndex: Int = 0
) {
    private var index = baseIndex

    fun map(arr: List<Selection<*>>): Any? =
        when (arr.size) {
            1 -> map(arr[0])
            2 -> map(arr[0]) to map(arr[1])
            3 -> Triple(map(arr[0]), map(arr[1]), map(arr[2]))
            4 -> Tuple4(map(arr[0]), map(arr[1]), map(arr[2]), map(arr[3]))
            5 -> Tuple5(map(arr[0]), map(arr[1]), map(arr[2]), map(arr[3]), map(arr[4]))
            6 -> Tuple6(map(arr[0]), map(arr[1]), map(arr[2]), map(arr[3]), map(arr[4]), map(arr[5]))
            7 -> Tuple7(map(arr[0]), map(arr[1]), map(arr[2]), map(arr[3]), map(arr[4]), map(arr[5]), map(arr[6]))
            8 -> Tuple8(map(arr[0]), map(arr[1]), map(arr[2]), map(arr[3]), map(arr[4]), map(arr[5]), map(arr[6]), map(arr[7]))
            9 -> Tuple9(map(arr[0]), map(arr[1]), map(arr[2]), map(arr[3]), map(arr[4]), map(arr[5]), map(arr[6]), map(arr[7]), map(arr[8]))
            else -> throw IllegalArgumentException("selection count must between 1 and 9")
        }

    fun map(selection: Selection<*>): Any? =
        when {
            selection is TableImpl<*, *> ->
                map(selection.entityType)
            selection is Expression<*> && selection.isSelectable ->
                read(selection.selectedType.kotlin)
            else ->
                error("Internal bug: Un-selectable expression has been selected")
        }

    private fun map(entityType: EntityType): Any? {
        val id = read(entityType.idProp.returnType)
        if (id === null) {
            index += entityType.starProps.size - 1
            return null
        }
        return produce(entityType.kotlinType) {
            Draft.set(this, entityType.idProp.immutableProp, id)
            for (prop in entityType.starProps.values) {
                if (!prop.isId) {
                    val targetType = prop.targetType
                    if (targetType !== null) {
                        val targetId = read(targetType.idProp.returnType)
                        val target = produce(targetType.kotlinType) {
                            Draft.set(this, targetType.idProp.immutableProp, targetId)
                        }
                        Draft.set(this, prop.immutableProp, target)
                    } else {
                        Draft.set(this, prop.immutableProp, read(prop.returnType))
                    }
                }
            }
        }
    }

    @SuppressWarnings("UNCHECKED_CAST")
    private fun read(type: KClass<*>): Any? {
        val value = read(index++)
        val scalarProvider = sqlClient.scalarProviderMap[type]
        val expectedJavaType = scalarProvider?.sqlType?.java ?: type.javaObjectType
        val sqlValue =
            if (value === null || value::class.java === expectedJavaType) {
                value
            } else {
                convert(value, expectedJavaType) ?:
                    throw ExecutionException(
                        "Failed the convert the result value at column ${index - 1}, " +
                            "the expected type is '${type.qualifiedName}', " +
                            "but the actual type is '${value::class.qualifiedName}'"
                    )
            }
        return if (scalarProvider !== null && sqlValue !== null) {
            (scalarProvider as ScalarProvider<Any, Any>).toScalar(sqlValue)
        } else {
            sqlValue
        }
    }

    private fun convert(value: Any, expectedType: Class<*>): Any? {

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

    protected abstract fun read(index: Int): Any?
}

internal class JdbcResultMapper(
    sqlClient: SqlClient,
    private val resultSet: ResultSet
): ResultMapper(sqlClient, 1) {

    override fun read(index: Int): Any? =
        resultSet.getObject(index)
}

internal class R2dbcResultMapper(
    sqlClient: SqlClient,
    private val row: Row
): ResultMapper(sqlClient) {

    override fun read(index: Int): Any? =
        row.get(index)
}
