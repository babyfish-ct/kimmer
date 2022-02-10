package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.Storage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class EntityMappingBuilderImpl: EntityMappingBuilder {

    private val entityTypeMap = mutableMapOf<ImmutableType, EntityTypeImpl>()

    override fun tableName(type: KClass<out Immutable>, tableName: String) {
        this[ImmutableType.of(type)].tableName = tableName
    }

    override fun id(prop: KProperty1<out Immutable, *>, storage: Storage?) {
        createProp(prop).apply {
            isId = true
            storage?.let {
                this.storage = it
            }
        }
    }

    override fun prop(prop: KProperty1<out Immutable, *>, storage: Storage?) {
        createProp(prop).apply {
            storage?.let {
                this.storage = it
            }
        }
    }

    override fun inverseAssociation(prop: KProperty1<out Immutable, *>, mappedBy: KProperty1<out Immutable, *>) {
        createProp(prop).apply {
            if (!immutableProp.isAssociation) {
                throw MappingException("Cannot map the non-association prop '$prop' as association prop")
            }
            setMappedByName(mappedBy.name)
        }
    }

    override fun storage(prop: KProperty1<out Immutable, *>, storage: Storage) {
        getProp(prop).apply {
            this.storage = storage
        }
    }

    override fun build(): Map<KClass<out Immutable>, EntityType> {
        for (phase in ResolvingPhase.values()) {
            resolve(phase)
        }
        val map = entityTypeMap.entries.associateBy({it.key.kotlinType}) {it.value }
        entityTypeMap.clear()
        return map
    }

    private fun resolve(phase: ResolvingPhase) {
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, phase)
        }
    }

    operator fun get(immutableType: ImmutableType): EntityTypeImpl =
        entityTypeMap[immutableType] ?: create(immutableType)

    private fun get(prop: KProperty1<out Immutable, *>): EntityTypeImpl {
        val type = prop.parameters[0].type.classifier as? KClass<*>
            ?: throw MappingException("Cannot map '$prop' because it does not belong to class")
        val immutableType = ImmutableType.of(type as KClass<out Immutable>)
        val entityType = try {
            get(immutableType)
        } catch (ex: Throwable) {
            throw MappingException(
                "Cannot map '$prop' because its declaring type '${type}' cannot be mapped",
                ex
            )
        }
        return entityType
    }

    private fun create(immutableType: ImmutableType): EntityTypeImpl {
        if (Input::class.java.isAssignableFrom(immutableType.kotlinType.java)) {
            throw MappingException(
                "'${immutableType.kotlinType.qualifiedName}' is not valid entity type, " +
                    "it can not be derived type of '${Input::class.qualifiedName}'"
            )
        }
        if (Connection::class.java.isAssignableFrom(immutableType.kotlinType.java)) {
            throw MappingException(
                "'${immutableType.kotlinType.qualifiedName}' is not valid entity type, " +
                    "it can not be derived type of '${Connection::class.qualifiedName}'"
            )
        }
        val entityType = EntityTypeImpl(immutableType)
        entityTypeMap[immutableType] = entityType
        for (superType in immutableType.superTypes) {
            this[superType]
        }
        for (prop in immutableType.declaredProps.values) {
            prop.targetType?.let {
                this[it]
            }
        }
        return entityType
    }

    private fun getProp(prop: KProperty1<out Immutable, *>): EntityPropImpl =
        get(prop).declaredProps[prop.name] ?: error("Cannot configure the prop '$prop' because it has not been mapped")

    @Suppress("UNCHECKED_CAST")
    private fun createProp(prop: KProperty1<out Immutable, *>): EntityPropImpl {
        val entityType = get(prop)
        if (entityType.declaredProps.containsKey(prop.name)) {
            throw MappingException("Cannot map '$prop' because its already been mapped")
        }
        val entityProp = EntityPropImpl(entityType, prop)
        entityType.declaredProps[prop.name] = entityProp
        return entityProp
    }
}