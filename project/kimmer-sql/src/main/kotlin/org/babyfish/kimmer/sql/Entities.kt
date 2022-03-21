package org.babyfish.kimmer.sql

import org.babyfish.kimmer.sql.ast.Executable
import kotlin.reflect.KClass

interface Entities {

    fun <E: Entity<*>> saveCommand(
        entity: E,
        block: (SaveOptionsDSL<E>.() -> Unit)? = null
    ): Executable<EntityMutationResult>

    fun <E: Entity<*>> saveCommand(
        entities: List<E>,
        block: (SaveOptionsDSL<E>.() -> Unit)? = null
    ): Executable<List<EntityMutationResult>>

    fun <E: Entity<ID>, ID: Comparable<ID>> deleteCommand(
        type: KClass<E>,
        id: ID
    ): Executable<EntityMutationResult>

    fun <E: Entity<ID>, ID: Comparable<ID>> deleteCommand(
        type: KClass<E>,
        ids: Collection<ID>
    ): Executable<List<EntityMutationResult>>
}

interface MutationResult {
    val totalAffectedRowCount: Int
}

interface EntityMutationResult : MutationResult {
    val entity: Entity<*>
    val type: MutationType
    val affectedRowCount: Int
    val associations: Collection<AssociationMutationResult>
}

interface AssociationMutationResult : MutationResult {
    val associationName: String
    val middleTableAffectedRowCount: Int
    val middleTableInsertedRowCount: Int
    val middleTableDeletedRowCount: Int
    val targets: List<AssociatedTargetMutationResult>
    val detachedTargets: List<AssociatedTargetMutationResult>
}

interface AssociatedTargetMutationResult: EntityMutationResult {
    val middleTableChanged: Boolean
}

enum class MutationType {
    NONE,
    INSERT,
    UPDATE,
    DELETE
}