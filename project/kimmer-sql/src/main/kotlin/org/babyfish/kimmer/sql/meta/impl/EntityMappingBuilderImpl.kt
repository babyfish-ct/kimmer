package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.meta.config.UserIdGenerator
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import org.babyfish.kimmer.sql.meta.spi.EntityTypeImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class EntityMappingBuilderImpl(
    private val metaFactory: MetaFactory
): EntityMappingBuilder {

    private val entityTypeMap = mutableMapOf<ImmutableType, EntityTypeImpl>()

    private val scalarProviderMap = mutableMapOf<KClass<*>, ScalarProvider<*, *>>()

    override fun entity(type: KClass<out Entity<*>>): EntityTypeImpl =
        this[ImmutableType.of(type)]

    override fun tableName(type: KClass<out Entity<*>>, tableName: String) {
        this[ImmutableType.of(type)].setTableName(tableName)
    }

    override fun prop(
        prop: KProperty1<out Entity<*>, *>,
        storage: Storage?,
        idGenerator: IdGenerator?,
        isVersion: Boolean
    ): EntityPropImpl =
        createProp(prop).apply {
            storage?.let {
                setStorage(it)
            }
            idGenerator?.let {
                setIdGenerator(it)
            }
            if (isVersion) {
                setVersion()
            }
        }

    override fun <ID : Comparable<ID>> prop(
        prop: KProperty1<out Entity<ID>, ID>,
        storage: Storage?,
        idGenerator: UserIdGenerator<ID>,
        isVersion: Boolean
    ): EntityPropImpl =
        prop(prop, storage, idGenerator as IdGenerator, isVersion)

    override fun inverseProp(
        prop: KProperty1<out Entity<*>, *>,
        mappedBy: KProperty1<out Entity<*>, *>
    ): EntityPropImpl =
        createProp(prop).apply {
            if (!immutableProp.isAssociation) {
                throw MappingException("Cannot map the non-association prop '$prop' as association prop")
            }
            setMappedByName(mappedBy.name)
        }

    override fun storage(prop: KProperty1<out Entity<*>, *>, storage: Storage) {
        getProp(prop).setStorage(storage)
    }

    override fun scalarProvider(scalarProvider: ScalarProvider<*, *>) {
        val type = scalarProvider.scalarType
        if (type.javaPrimitiveType !== null) {
            throw IllegalArgumentException(
                "Illegal ScalarTypeProvider, it's scalar type cannot be primitive types of their box types"
            )
        }
        if (type == String::class) {
            throw IllegalArgumentException(
                "Illegal ScalarTypeProvider, it's scalar type cannot be String"
            )
        }
        if (Immutable::class.java.isAssignableFrom(type.java)) {
            throw IllegalArgumentException(
                "Illegal ScalarTypeProvider, it's scalar type cannot inherits '${Immutable::class.qualifiedName}'"
            )
        }
        if (scalarProviderMap.containsKey(type)) {
            throw MappingException("The enum provider of '${type}' has been registered")
        }
        scalarProviderMap[type] = scalarProvider
    }

    @Suppress("UNCHECKED_CAST")
    fun build(): Pair<
        Map<KClass<out Entity<*>>, EntityType>,
        Map<KClass<*>, ScalarProvider<*, *>>
    > {
        for (phase in ResolvingPhase.values()) {
            resolve(phase)
        }
        val map = entityTypeMap.entries.associateBy(
            {it.key.kotlinType as KClass<out Entity<*>>}
        ) {
            it.value
        }
        entityTypeMap.clear()
        return map to scalarProviderMap
    }

    private fun resolve(phase: ResolvingPhase) {
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, phase)
        }
    }

    operator fun get(immutableType: ImmutableType): EntityTypeImpl =
        entityTypeMap[immutableType] ?: create(immutableType)

    fun scalarProvider(type: KClass<*>): ScalarProvider<*, *>? =
        scalarProviderMap[type]

    @Suppress("UNCHECKED_CAST")
    private fun get(prop: KProperty1<out Entity<*>, *>): EntityTypeImpl {
        val type = prop.parameters[0].type.classifier as? KClass<*>
            ?: throw MappingException("Cannot map '$prop' because it does not belong to class")
        val immutableType = ImmutableType.of(type as KClass<out Entity<*>>)
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
        val entityType = metaFactory.createEntityType(immutableType)
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

    private fun getProp(prop: KProperty1<out Entity<*>, *>): EntityPropImpl =
        get(prop).mutableDeclaredProps[prop.name] ?: error("Cannot configure the prop '$prop' because it has not been mapped")

    @Suppress("UNCHECKED_CAST")
    private fun createProp(prop: KProperty1<out Entity<*>, *>): EntityPropImpl {
        val entityType = get(prop)
        if (entityType.declaredProps.containsKey(prop.name)) {
            throw MappingException("Cannot map '$prop' because its already been mapped")
        }
        val entityProp = metaFactory.createEntityProp(entityType, prop)
        entityType.mutableDeclaredProps[prop.name] = entityProp
        return entityProp
    }
}