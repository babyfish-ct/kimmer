package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.SaveOptionsDSLImpl
import org.babyfish.kimmer.sql.ast.Executable
import kotlin.reflect.KClass

internal class EntitiesImpl(
    private val sqlClient: SqlClient
): Entities {

    override fun <E : Entity<*>> saveCommand(
        entity: E,
        block: (SaveOptionsDSL<E>.() -> Unit)?
    ): Executable<EntityMutationResult> {
        val type = ImmutableType.fromInstance(entity).kotlinType
        val entityType = sqlClient.entityTypeMap[type]
            ?: throw IllegalArgumentException("${type.qualifiedName} is entity type of current sql client")
        val saveOptions = SaveOptionsDSLImpl<E>(entityType, insertable = true, updatable = true, deletable = false).run {
            if (block !== null) {
                block()
            }
            build()
        }
        return SaveCommand(sqlClient, entity, saveOptions)
    }

    override fun <E : Entity<*>> saveCommand(
        entities: List<E>,
        block: (SaveOptionsDSL<E>.() -> Unit)?
    ): Executable<List<EntityMutationResult>> {
        if (entities.isEmpty()) {
            return CompositeSaveCommand(emptyList())
        }
        val commonType: KClass<out Immutable> = ImmutableType.fromInstance(entities[0]).kotlinType
        for (entity in entities.subList(1, entities.size)) {
            val type = ImmutableType.fromInstance(entity).kotlinType
            if (type !== commonType) {
                throw IllegalArgumentException(
                    "entities contains different types: " +
                        "'${commonType.qualifiedName}' and '${type.qualifiedName}'"
                )
            }
        }
        val entityType = sqlClient.entityTypeMap[commonType]
            ?: throw IllegalArgumentException("${commonType.qualifiedName} is entity type of current sql client")
        val saveOptions = SaveOptionsDSLImpl<E>(entityType, insertable = true, updatable = true, deletable = false).run {
            if (block !== null) {
                block()
            }
            build()
        }
        return CompositeSaveCommand(entities.map { SaveCommand(sqlClient, it, saveOptions) })
    }

    override fun <E : Entity<ID>, ID : Comparable<ID>> deleteCommand(
        type: KClass<E>,
        id: ID
    ): Executable<EntityMutationResult> =
        SimpleDeleteCommand(DeleteCommand(sqlClient, type, listOf(id)))

    override fun <E : Entity<ID>, ID : Comparable<ID>> deleteCommand(
        type: KClass<E>,
        ids: Collection<ID>
    ): Executable<List<EntityMutationResult>> =
        DeleteCommand(sqlClient, type, ids)
}