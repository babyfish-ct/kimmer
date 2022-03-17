package org.babyfish.kimmer.sql

import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.meta.EntityProp
import kotlin.reflect.KClass

interface Entities {

    fun <E: Entity<*>> saveCommand(
        entity: E,
        block: (SaveOptionsDSL<E>.() -> Unit)? = null
    ): Executable<RootMutationResult>

    fun <E: Entity<*>> saveCommand(
        entities: List<E>,
        block: (SaveOptionsDSL<E>.() -> Unit)? = null
    ): Executable<List<RootMutationResult>>

    fun <E: Entity<ID>, ID: Comparable<ID>> deleteCommand(
        type: KClass<E>,
        id: ID
    ): Executable<RootMutationResult>

    fun <E: Entity<ID>, ID: Comparable<ID>> deleteCommand(
        type: KClass<E>,
        ids: Collection<ID>
    ): Executable<List<RootMutationResult>>
}

interface MutationResult {
    val totalAffectedRowCount: Int
}

interface RootMutationResult : MutationResult {
    val entity: Entity<*>
    val type: MutationType
    val affectedRowCount: Int
    val associationMap: Map<String, AssociationMutationResult>
}

interface AssociationMutationResult : MutationResult {
    val middleTableAffectedRowCount: Int
    val middleTableInsertedRowCount: Int
    val middleTableDeletedRowCount: Int
    val targets: List<AssociatedTargetMutationResult>
    val detachedTargets: List<AssociatedTargetMutationResult>
}

interface AssociatedTargetMutationResult: RootMutationResult {
    val middleTableChanged: Boolean
}

enum class MutationType {
    NONE,
    INSERT,
    UPDATE,
    DELETE
}