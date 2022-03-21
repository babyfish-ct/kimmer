package org.babyfish.kimmer.sql.runtime

import com.fasterxml.jackson.annotation.JsonIncludeProperties
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@JsonIncludeProperties(value = [
    "totalAffectedRowCount",
    "type",
    "row",
    "affectedRowCount",
    "associations"
])
internal open class MutationContext private constructor(
    private var _entity: Entity<*>?,
    private val _entityId: Any?,
    val mutationOptions: MutationOptions
) : EntityMutationResult {

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

    val row: String
        get() = (_entity ?: error("Cannot get row because entity has not been not initialized"))
            .toString()

    val isEntityInitialized: Boolean
        get() = _entity !== null

    override var type: MutationType = MutationType.NONE

    private val _associationMap: MutableMap<String, AssociationContext> =
        ConcurrentHashMap()

    override val affectedRowCount: Int
        get() = if (type == MutationType.NONE) 0 else 1

    override val associations: Collection<AssociationContext>
        get() = _associationMap.values

    @Suppress("UNCHECKED_CAST")
    fun saveAssociation(
        prop: EntityProp,
        block: AssociationContext.() -> Unit
    ) {
        if (Immutable.isLoaded(entity, prop.immutableProp)) {
            _associationMap.computeIfAbsent(prop.name) {
                if (prop.targetType === null) {
                    error("Internal bug: '$prop' is not association")
                }
                val targets = Immutable.get(entity, prop.immutableProp).let {
                    when {
                        it is List<*> -> it as List<Entity<*>>
                        it === null -> emptyList()
                        else -> listOf(it as Entity<*>)
                    }
                }
                AssociationContext(prop, prop.opposite, targets)
            }.apply {
                block()
                close()
            }
        }
    }

    fun deleteAssociation(
        prop: EntityProp,
        block: AssociationContext.() -> Unit
    ) {
        _associationMap.computeIfAbsent(prop.name) {
            AssociationContext(prop, prop.opposite, emptyList())
        }.apply {
            block()
            close()
        }
    }

    fun deleteAssociationByBackProp(
        backProp: EntityProp,
        block: AssociationContext.() -> Unit
    ) {
        _associationMap.computeIfAbsent(associationName(null, backProp)) {
            AssociationContext(backProp.opposite, backProp, emptyList())
        }.apply {
            block()
            close()
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun saveAssociationAsync(
        prop: EntityProp,
        block: suspend AssociationContext.() -> Unit
    ) {
        if (Immutable.isLoaded(entity, prop.immutableProp)) {
            _associationMap.computeIfAbsent(prop.name) {
                if (prop.targetType === null) {
                    error("Internal bug: '$prop' is not association")
                }
                val targets = Immutable.get(entity, prop.immutableProp).let {
                    when {
                        it is List<*> -> it as List<Entity<*>>
                        it === null -> emptyList()
                        else -> listOf(it as Entity<*>)
                    }
                }
                AssociationContext(prop, prop.opposite, targets)
            }.apply {
                block()
                close()
            }
        }
    }

    suspend fun deleteAssociationAsync(
        prop: EntityProp,
        block: suspend AssociationContext.() -> Unit
    ) {
        _associationMap.computeIfAbsent(prop.name) {
            AssociationContext(prop, prop.opposite, emptyList())
        }.apply {
            block()
            close()
        }
    }

    suspend fun deleteAssociationByBackPropAsync(
        backProp: EntityProp,
        block: suspend AssociationContext.() -> Unit
    ) {
        _associationMap.computeIfAbsent(associationName(null, backProp)) {
            AssociationContext(backProp.opposite, backProp, emptyList())
        }.apply {
            block()
            close()
        }
    }

    override val totalAffectedRowCount: Int
        get() = affectedRowCount + associations.sumOf { it.totalAffectedRowCount }

    override fun toString(): String =
        "{$contentString}"

    protected val contentString: String
        get() = "totalAffectedRowCount:$totalAffectedRowCount,type:$type,affectedRowCount:$affectedRowCount,entity:$entity,associations:[" +
            associations.joinToString(",") +
            "]"

    @JsonIncludeProperties(value = [
        "associationName",
        "totalAffectedRowCount",
        "targets",
        "detachedTargets",
        "middleTableInsertedRowCount",
        "middleTableDeletedRowCount"
    ])
    inner class AssociationContext(
        val prop: EntityProp?,
        val backProp: EntityProp?,
        targets: List<Entity<*>>
    ): AssociationMutationResult {

        init {
            if (prop === null && backProp === null) {
                error("Internal bug: Neither prop nor back prop is specified")
            }
        }

        override val associationName: String
            get() = prop?.name ?: "←${backProp!!.name}"

        val ownerType: EntityType
            get() = prop?.declaringType ?: backProp!!.targetType!!

        val targetType: EntityType
            get() = prop?.targetType ?: backProp!!.declaringType

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
                .targetMutationOptions.computeIfAbsent(associationName) {
                    MutationOptions(
                        targetType,
                        insertable = false,
                        updatable = true,
                        deletable = false,
                        keyProps = null,
                        targetMutationOptions = mutableMapOf()
                    )
                }

            if (targetMutationOptions.deletable) {
                if (backProp?.isReference != true) {
                    throw ExecutionException(
                        "Cannot enabled the 'deleteDetachedObject' of saveOptions of the association '$associationName', " +
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
            val targetKotlinType = targetType.kotlinType as KClass<Entity<*>>
            val targetIdProp = targetType.idProp
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
            if (prop !== null) {
                entity = if (prop.isReference) {
                    val targetEntity = targets.firstOrNull()?.let { it.entity }
                    produce(mutationOptions.entityType.kotlinType as KClass<Entity<*>>, entity) {
                        Draft.set(this, prop.immutableProp, targetEntity)
                    }
                } else {
                    val targetEntities = targets.map { it.entity }
                    produce(mutationOptions.entityType.kotlinType as KClass<Entity<*>>, entity) {
                        Draft.set(this, prop.immutableProp, targetEntities)
                    }
                }
            }
        }

        override val totalAffectedRowCount: Int
            get() =
                targets.sumOf { it.totalAffectedRowCount } +
                    detachedTargets.sumOf { it.totalAffectedRowCount }

        override fun toString(): String =
            "{associationName:\"$associationName\"," +
                "totalAffectedRowCount:$totalAffectedRowCount," +
                "targets:[${targets.joinToString(",")}]," +
                "detachedTargets:[${detachedTargets.joinToString(",")}]," +
                "middleTableInsertedRowCount:$middleTableInsertedRowCount," +
                "middleTableDeletedRowCount:$middleTableDeletedRowCount}"

        @JsonIncludeProperties(value = [
            "totalAffectedRowCount",
            "type",
            "row",
            "affectedRowCount",
            "associations",
            "middleTableChanged"
        ])
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