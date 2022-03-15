package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.meta.EntityProp
import kotlin.reflect.KClass

internal open class MutationContext private constructor(
    private var _entity: Entity<*>?,
    private val _entityId: Any?,
    val mutationOptions: MutationOptions
) : RootMutationResult {

    constructor(entity: Entity<*>, mutationOptions: MutationOptions)
        : this(entity, null, mutationOptions)

    constructor(entityId: Any, mutationOptions: MutationOptions)
        : this(null, entityId, mutationOptions)

    val entityId: Any
        get() = _entityId
            ?: _entity?.id
            ?: error("Internal bug: neither entity nor entityId is initialized")

    override var entity: Entity<*>
        get() = _entity ?: error("Internal bug: entity has not been initialized")
        set(value) {
            _entity = value
        }

    val isEntityInitialized: Boolean
        get() = _entity !== null

    override var type: MutationType = MutationType.NONE

    private val _associationMap: MutableMap<EntityProp, AssociationContext> =
        mutableMapOf()

    override val affectedRowCount: Int
        get() = if (type == MutationType.NONE) 0 else 1

    override val associationMap: Map<EntityProp, AssociationContext>
        get() = _associationMap

    @Suppress("UNCHECKED_CAST")
    fun saveAssociation(
        entityProp: EntityProp,
        block: AssociationContext.() -> Unit
    ) {
        if (Immutable.isLoaded(entity, entityProp.immutableProp)) {
            _associationMap.computeIfAbsent(entityProp) {
                if (entityProp.targetType === null) {
                    error("Internal bug: '$entityProp' is not association")
                }
                val targets = Immutable.get(entity, entityProp.immutableProp).let {
                    when {
                        it is List<*> -> it as List<Entity<*>>
                        it === null -> emptyList()
                        else -> listOf(it as Entity<*>)
                    }
                }
                AssociationContext(entityProp, targets)
            }.apply {
                block()
                close()
            }
        }
    }

    fun deleteAssociation(
        entityProp: EntityProp,
        block: AssociationContext.() -> Unit
    ) {
        _associationMap.computeIfAbsent(entityProp) {
            if (entityProp.targetType === null) {
                error("Internal bug: '$entityProp' is not association")
            }
            AssociationContext(entityProp, emptyList())
        }.apply {
            block()
            close()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun saveAssociationAsync(
        entityProp: EntityProp,
        block: suspend AssociationContext.() -> Unit
    ) {
        if (Immutable.isLoaded(entity, entityProp.immutableProp)) {
            _associationMap.computeIfAbsent(entityProp) {
                if (entityProp.targetType === null) {
                    error("Internal bug: '$entityProp' is not association")
                }
                val targets = Immutable.get(entity, entityProp.immutableProp).let {
                    when {
                        it is List<*> -> it as List<Entity<*>>
                        it === null -> emptyList()
                        else -> listOf(it as Entity<*>)
                    }
                }
                AssociationContext(entityProp, targets)
            }.apply {
                block()
                close()
            }
        }
    }

    suspend fun deleteAssociationAsync(
        entityProp: EntityProp,
        block: suspend AssociationContext.() -> Unit
    ) {
        _associationMap.computeIfAbsent(entityProp) {
            if (entityProp.targetType === null) {
                error("Internal bug: '$entityProp' is not association")
            }
            AssociationContext(entityProp, emptyList())
        }.apply {
            block()
            close()
        }
    }

    override val totalAffectedRowCount: Int
        get() = affectedRowCount + associationMap.values.sumOf { it.totalAffectedRowCount }

    override fun toString(): String =
        "{$contentString}"

    protected val contentString: String
        get() = "totalAffectedRowCount:$totalAffectedRowCount,type:$type,affectedRowCount:$affectedRowCount,entity:$entity,associationMap:{" +
            associationMap.entries.joinToString(",") {
                "${it.key.name}:${it.value}"
            } +
            "}"

    inner class AssociationContext(
        val entityProp: EntityProp,
        targets: List<Entity<*>>
    ): AssociationMutationResult {

        val targetMutationOptions: MutationOptions

        private var entityChanged = false

        private var _middleTableInsertedRowCount = 0

        private var _middleTableDeletedRowCount = 0

        private var _detachedTargets: List<TargetContext>? = null

        override val middleTableInsertedRowCount: Int
            get() = _middleTableInsertedRowCount

        override val middleTableDeletedRowCount: Int
            get() = _middleTableDeletedRowCount

        override val middleTableAffectedRowCount: Int
            get() = _middleTableInsertedRowCount + middleTableDeletedRowCount

        override val targets: List<TargetContext>

        override val detachedTargets: List<TargetContext>
            get() = _detachedTargets ?: emptyList()

        val owner: MutationContext
            get() = this@MutationContext

        init {
            targetMutationOptions = mutationOptions
                .targetMutationOptions.computeIfAbsent(entityProp) {
                    MutationOptions(
                        entityProp.targetType
                            ?: throw IllegalArgumentException("$entityProp is not association"),
                        insertable = false,
                        updatable = true,
                        deletable = false,
                        keyProps = null,
                        targetMutationOptions = mutableMapOf()
                    )
                }

            if (targetMutationOptions.deletable) {
                if (entityProp.mappedBy?.isReference != true) {
                    throw ExecutionException(
                        "Cannot enabled the 'deleteDetachedObject' of saveOptions of the association '$entityProp', " +
                            "that's not one-to-many association"
                    )
                }
            }
            this.targets = targets.map {
                TargetContext(it, targetMutationOptions)
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun detachByTargetIds(targetIds: Collection<Any>) {
            if (_detachedTargets !== null) {
                error("The detachTargets of current AssociationContext has been set")
            }
            val targetKotlinType = entityProp.targetType!!.kotlinType as KClass<Entity<*>>
            val targetIdProp = entityProp.targetType!!.idProp
            _detachedTargets = targetIds
                .map {
                    produce(targetKotlinType) {
                        Draft.set(this, targetIdProp.immutableProp, it)
                    }
                }
                .map {
                    TargetContext(it, targetMutationOptions)
                }
        }

        fun detachByTargets(targets: Collection<Entity<*>>) {
            if (_detachedTargets !== null) {
                error("The detachTargets of current AssociationContext has been set")
            }
            _detachedTargets = targets
                .map {
                    TargetContext(it, targetMutationOptions)
                }
        }

        @Suppress("UNCHECKED_CAST")
        fun close() {
            if (!entityChanged) {
                return
            }
            entity = if (entityProp.isReference) {
                val targetEntity = targets.firstOrNull()?.let { it.entity }
                produce(mutationOptions.entityType.kotlinType as KClass<Entity<*>>, entity) {
                    Draft.set(this, entityProp.immutableProp, targetEntity)
                }
            } else {
                val targetEntities = targets.map { it.entity }
                produce(mutationOptions.entityType.kotlinType as KClass<Entity<*>>, entity) {
                    Draft.set(this, entityProp.immutableProp, targetEntities)
                }
            }
        }

        override val totalAffectedRowCount: Int
            get() =
                targets.sumOf { it.totalAffectedRowCount } +
                    detachedTargets.sumOf { it.totalAffectedRowCount }

        override fun toString(): String =
            "{totalAffectedRowCount:$totalAffectedRowCount," +
                "targets:[${targets.joinToString(",")}]," +
                "detachedTargets:[${detachedTargets.joinToString(",")}]," +
                "middleTableInsertedRowCount:$middleTableInsertedRowCount," +
                "middleTableDeletedRowCount:$middleTableDeletedRowCount}"

        inner class TargetContext(
            target: Entity<*>,
            mutationOptions: MutationOptions
        ): MutationContext(target, mutationOptions), AssociatedTargetMutationResult {

            private var _middleTableChanged = false

            override var entity: Entity<*>
                get() = super.entity
                @Suppress("UNCHECKED_CAST") set(value) {
                    if (super.entity !== value) {
                        super.entity = value
                        entityChanged = true
                    }
                }

            override val middleTableChanged: Boolean
                get() = _middleTableChanged

            fun middleTableInserted() {
                if (!_middleTableChanged) {
                    _middleTableChanged = true
                    _middleTableInsertedRowCount++
                }
            }

            fun middleTableDeleted() {
                if (!_middleTableChanged) {
                    _middleTableChanged = true
                    _middleTableDeletedRowCount++
                }
            }

            override val totalAffectedRowCount: Int
                get() = super.totalAffectedRowCount + if (middleTableChanged) 1 else 0

            override fun toString(): String =
                "{$contentString,middleTableChanged:$middleTableChanged}"
        }
    }
}