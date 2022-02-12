package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.spi.databaseIdentifier

internal class EntityTypeImpl(
    override val immutableType: ImmutableType
): EntityType {

    init {
        if (!Entity::class.java.isAssignableFrom(immutableType.kotlinType.java)) {
            throw IllegalArgumentException(
                "Cannot create entity type of '${immutableType.kotlinType.qualifiedName}' " +
                    "because it does inherit '${Entity::class.qualifiedName}'"
            )
        }
    }

    private var _superType: EntityTypeImpl? = null

    private val _derivedTypes = mutableListOf<EntityTypeImpl>()

    private var _props: Map<String, EntityProp>? = null

    private var _idProp: EntityProp? = null

    private var _tableName: String? = null

    private var _expectedPhase = ResolvingPhase.SUPER_TYPE.ordinal

    override val name: String
        get() = immutableType.simpleName

    override var isMapped: Boolean = false

    override var tableName: String
        get() = _tableName ?: databaseIdentifier(kotlinType.simpleName!!)
        set(value) {
            if (_tableName !== null) {
                throw MappingException(
                    "Cannot set the table name of ${kotlinType.qualifiedName} " +
                        "because it has already been set"
                )
            }
            _tableName = value
        }

    override val superType: EntityType?
        get() = _superType

    override val derivedTypes: List<EntityType>
        get() = _derivedTypes

    override val idProp: EntityProp
        get() = _idProp ?: error("Id property has not been resolved")

    override val declaredProps = sortedMapOf<String, EntityPropImpl>()

    override val props: Map<String, EntityProp>
        get() = _props ?: error("Properties have not been resolved")

    override val starProps: Map<String, EntityProp> by lazy {
        // select * from table
        props
            .values
            .filter {
                // Ignore middle table because there are expensive
                it.storage is Column || it.storage is Formula
            }
            .associateBy { it.name }
    }

    fun resolve(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        if (shouldResolve(phase)) {
            when (phase) {
                ResolvingPhase.SUPER_TYPE -> resolveSuperTypes(builder)
                ResolvingPhase.DECLARED_PROPS -> resolveDeclaredProps(builder)
                ResolvingPhase.PROPS -> resolveProps(builder)
                ResolvingPhase.ID_PROP -> resolveIdProp()
                else -> resolvePropDetail(builder, phase)
            }
        }
    }

    private fun shouldResolve(phase: ResolvingPhase): Boolean =
        if (_expectedPhase == phase.ordinal) {
            _expectedPhase++
            true
        } else {
            false
        }

    private fun resolveSuperTypes(builder: EntityMappingBuilderImpl) {
        for (superImmutableType in immutableType.superTypes) {
            if (Entity::class.java.isAssignableFrom(superImmutableType.kotlinType.java)) {
                val superType = builder[superImmutableType]
                if (_superType !== null) {
                    throw MappingException(
                        "Illegal entity type '${immutableType.kotlinType.qualifiedName}', " +
                            "only one super interface is allowed to inherit '${Entity::class.qualifiedName}', " +
                            "but two super interfaces inherit it: '${_superType!!.immutableType.kotlinType.qualifiedName}' " +
                            "and '${superType.immutableType.kotlinType.qualifiedName}'"
                    )
                }
                _superType = superType
                superType._derivedTypes += this
            }
        }
    }

    private fun resolveDeclaredProps(builder: EntityMappingBuilderImpl) {
        for (immutableProp in immutableType.declaredProps.values) {
            if (!declaredProps.containsKey(immutableProp.name)) {
                if (immutableProp.isAssociation) {
                    throw MappingException(
                        "The property '${immutableProp}' is association " +
                            "but it is not mapped"
                    )
                }
                declaredProps[immutableProp.name] = EntityPropImpl(
                    this,
                    immutableProp.kotlinProp
                )
            }
        }
    }

    private fun resolveProps(builder: EntityMappingBuilderImpl) {
        val sp = _superType
        if (sp === null) {
            _props = declaredProps
        } else {
            sp.resolve(builder, ResolvingPhase.PROPS)
            val map = sortedMapOf<String, EntityProp>()
            map += declaredProps
            if (builder[sp.immutableType].isMapped) {
                for (superProp in sp.props.values) {
                    val prop = map[superProp.name]
                    if (prop !== null) {
                        if (!superProp.isId) {
                            throw MappingException(
                                "Duplicate properties: '$superProp' and '$prop'"
                            )
                        }
                    } else {
                        map[superProp.name] = superProp
                    }
                }
            }
            _props = map
        }
    }

    private fun resolvePropDetail(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        for (declaredProp in declaredProps.values) {
            declaredProp.resolve(builder, phase)
        }
    }

    private fun resolveIdProp(): EntityProp? {
        val idProp = _superType?.resolveIdProp()
            ?: declaredProps
                .values
                .first { it.isId }
        _idProp = idProp
        return idProp
    }

    override fun toString(): String =
        immutableType.qualifiedName
}
