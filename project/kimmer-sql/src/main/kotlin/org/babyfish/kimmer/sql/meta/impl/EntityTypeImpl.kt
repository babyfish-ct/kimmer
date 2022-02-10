package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.spi.databaseIdentifier

internal class EntityTypeImpl(
    override val immutableType: ImmutableType
): EntityType {

    private val _superTypes = mutableListOf<EntityTypeImpl>()

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
        }

    override val superTypes: List<EntityType>
        get() = _superTypes

    override val derivedTypes: List<EntityType>
        get() = _derivedTypes

    override val idProp: EntityProp
        get() = _idProp ?: error("Id property has not been resolved")

    override val declaredProps = mutableMapOf<String, EntityPropImpl>()

    override val props: Map<String, EntityProp>
        get() = _props ?: error("Properties have not been resolved")

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
            val superType = builder[superImmutableType]
            _superTypes += superType
            superType._derivedTypes += this
        }
    }

    private fun resolveDeclaredProps(builder: EntityMappingBuilderImpl) {
        for (immutableProp in immutableType.declaredProps.values) {
            if (!declaredProps.containsKey(immutableProp.name)) {
                if (immutableProp.isAssociation) {
                    throw MappingException(
                        "The property '${immutableProp}' is association " +
                            "but it is not configured by any EntityAssembler"
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
        for (superType in _superTypes) {
            superType.resolve(builder, ResolvingPhase.PROPS)
        }
        if (_superTypes.isEmpty()) {
            _props = declaredProps
        } else {
            val map = mutableMapOf<String, EntityProp>()
            map += declaredProps
            for (superType in _superTypes) {
                if (builder[superType.immutableType].isMapped) {
                    for (superProp in superType.props.values) {
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

        if (superTypes.isNotEmpty()) {
            var superIdProp: EntityProp? = null
            for (superType in _superTypes) {
                var prop = superType.resolveIdProp()
                if (superIdProp !== null) {
                    throw MappingException(
                        "'${this}' inherits two id properties:" +
                            "'$superIdProp' and '$prop'"
                    )
                }
                superIdProp = prop
            }
            if (superIdProp !== null) {
                _idProp = superIdProp
                return _idProp
            }
        }
        val idProps = declaredProps.values.filter { it.isId }
        if (idProps.size > 1) {
            throw MappingException(
                "More than one 1 id properties is specified for type '${immutableType}'"
            )
        }
        _idProp = idProps.firstOrNull()
        return _idProp
    }

    override fun toString(): String =
        immutableType.qualifiedName
}
