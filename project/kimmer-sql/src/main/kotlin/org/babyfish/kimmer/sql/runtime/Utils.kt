package org.babyfish.kimmer.sql.runtime

import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.meta.EntityProp
import java.sql.PreparedStatement
import java.sql.ResultSet

internal const val JDBC_BASE_INDEX = 1

internal const val R2DBC_BASE_INDEX = 0

internal fun propEqual(
    prop: EntityProp,
    entity1: Entity<*>,
    entity2: Entity<*>
): Boolean {
    val v1 = Immutable.get(entity1, prop.immutableProp)
    val v2 = Immutable.get(entity2, prop.immutableProp)
    if (prop.isReference) {
        if (v1 === v2) {
            return true
        }
        if (v1 === null || v2 === null) {
            return false
        }
        return (v1 as Entity<*>).id == (v2 as Entity<*>).id
    } else {
        return v1 == v2
    }
}

internal fun <E> List<E>.toLimitString(
    limit: Int = 10,
    transform: ((E) -> CharSequence)? = null
): String {
    val list = if (size > limit) {
        subList(0, limit)
    } else {
        this
    }
    return list.joinToString(
        prefix = "[",
        postfix = if (size > limit) ", ...]" else "]",
        transform = transform
    )
}

internal fun <R> PreparedStatement.mapRows(block: ResultSet.() -> R): List<R> =
    executeQuery().use {
        mutableListOf<R>().apply {
            while (it.next()) {
                this += it.block()
            }
        }
    }

@Suppress("UNCHECKED_CAST")
internal suspend fun <R> Result.mapRows(block: Row.() -> R): List<R> =
    map { row, _ ->
        row.block() ?: Null // Why "asFlow" requires "T: Any"?
    }
    .asFlow()
    .toList()
    .map { if (it === Null) null else it } as List<R>

private object Null

internal fun Row.getObject(index: Int): Any =
    get(index) ?: ExecutionException("The value of column $index should not be null")

internal fun associationName(prop: EntityProp?, backProp: EntityProp?): String =
    prop?.name
        ?: backProp?.opposite?.name
        ?: "←${backProp?.name ?: error("Internal bug neither prop nor backProp is specified")}"