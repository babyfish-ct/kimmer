package org.babyfish.kimmer.sql.meta.spi

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.config.*
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.meta.impl.ResolvingPhase
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class EntityPropImpl(
    private val _declaringType: EntityType,
    kotlinProp: KProperty1<*, *>
): EntityProp {

    private var _isTransient: Boolean = false

    private var _mappedBy: Any? = null

    private var _opposite: EntityProp? = null

    private var _scalarProvider: ScalarProvider<*, *>? = null

    private var _storage: Storage? = null

    private var _targetType: EntityType? = null

    override val declaringType: EntityType
        get() = _declaringType

    override val immutableProp: ImmutableProp = _declaringType.immutableType.props[kotlinProp.name]
        ?.also {
            if (it.isScalarList) {
                throw IllegalArgumentException(
                    "Illegal property '${it}', entity does not support scalar list property"
                )
            }
        }
        ?: throw IllegalArgumentException(
            "No property '${kotlinProp.name}' of type '${_declaringType.kotlinType.qualifiedName}'"
        )

    override val isId: Boolean
        get() = kotlinProp.name == Entity<*>::id.name

    override val isVersion: Boolean
        get() = this === declaringType.versionProp

    override val isTransient: Boolean
        get() = _isTransient

    override val targetType: EntityType?
        get() = _targetType

    override val scalarProvider: ScalarProvider<*, *>?
        get() = _scalarProvider

    override val storage: Storage?
        get() = _storage

    override val returnType: KClass<*>
        get() = immutableProp.returnType

    override val javaReturnType: Class<*>
        get() = immutableProp.javaReturnType

    override val isReference: Boolean
        get() = immutableProp.isReference

    override val isList: Boolean
        get() = immutableProp.isList

    override val isConnection: Boolean
        get() = immutableProp.isConnection

    override val isAssociation: Boolean
        get() = immutableProp.isAssociation

    override val isNullable: Boolean
        get() = immutableProp.isNullable

    override val isTargetNullable: Boolean
        get() = immutableProp.isElementNullable

    override val mappedBy: EntityProp?
        get() = _mappedBy as EntityProp?

    override val opposite: EntityProp?
        get() = _opposite

    internal fun setTransient() {
        _isTransient = true
    }

    internal fun setMappedByName(name: String) {
        if (_isTransient) {
            throw MappingException(
                "Cannot set mappedBy of '${kotlinProp}' " +
                    "because it's transient"
            )
        }
        if (_mappedBy !== null) {
            throw MappingException(
                "Cannot set mappedBy of '${kotlinProp}' " +
                    "because it has already been set"
            )
        }
        _mappedBy = name
    }

    internal fun setStorage(storage: Storage) {
        if (_isTransient) {
            throw MappingException(
                "Cannot set storagge of '${kotlinProp}' " +
                    "because it's transient"
            )
        }
        if (_mappedBy !== null) {
            throw MappingException("Cannot configure storage for the inverse association '$kotlinProp'")
        }
        if ((isList || isConnection) && storage !is MiddleTable) {
            throw MappingException("list/connection association '$kotlinProp' only accept middle table")
        }
        if (isReference && storage is Formula<*, *, *>) {
            throw MappingException("reference association '$kotlinProp' does not accept formula")
        }
        if (storage is Column && storage.onDelete != OnDeleteAction.NONE) {
            if (!isReference) {
                throw MappingException("Cannot set 'onDelete' from '$kotlinProp' because it is not reference")
            }
            if (storage.onDelete == OnDeleteAction.SET_NULL && !isNullable) {
                throw MappingException("Cannot set 'onDelete' to 'SET_NULL' from '$kotlinProp' because it is not nullable")
            }
        }
        _storage = if (storage is Column && storage.name == "") {
            storage.copy(
                name = if (isReference) {
                    databaseIdentifier("${name}_id")
                } else {
                    databaseIdentifier(name)
                }
            )
        } else {
            storage
        }
    }

    internal fun resolve(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.PROP_SCALAR_PROVIDER -> resolveScalarProvider(builder)
            ResolvingPhase.PROP_TARGET -> resolveTarget(builder)
            ResolvingPhase.PROP_MAPPED_BY -> resolvedMappedBy(builder)
            ResolvingPhase.PROP_DEFAULT_COLUMN -> resolveDefaultColumn()
            ResolvingPhase.PROP_TARGET_TYPE_BACK_PROPS -> resolveTargetTypeBackProps()
            ResolvingPhase.ON_INITIALIZE_SPI -> onInitialize()
        }
    }

    private fun resolveScalarProvider(builder: EntityMappingBuilderImpl) {
        _scalarProvider = builder.scalarProvider(returnType)
        if (returnType.java.isEnum && _scalarProvider === null) {
            throw MappingException(
                "The property '${kotlinProp}' returns enum type '${returnType}', " +
                    "but there is no scalar provider for that enum type"
            )
        }
    }

    private fun resolveTarget(builder: EntityMappingBuilderImpl) {
        immutableProp.targetType?.let {
            val tgtType = builder[it]
            _targetType = tgtType
        }
    }

    private fun resolvedMappedBy(builder: EntityMappingBuilderImpl) {
        if (_mappedBy is String) {
            val mappedByProp = (_targetType!!.props[_mappedBy] as EntityPropImpl?)
                ?: throw MappingException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}', " +
                        "but there is not property named '${_mappedBy}' in the target type '${_targetType}' "
                )
            if (mappedByProp._targetType !== declaringType) {
                throw MappingException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${_targetType}' is not " +
                        "association point to current type '${declaringType}'"
                )
            }
            if (mappedByProp._mappedBy !== null) {
                throw MappingException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${_targetType}' is specified " +
                        "with 'mappedBy' too, this is not allowed"
                )
            }
            _mappedBy = mappedByProp
            _opposite = mappedByProp
            mappedByProp._opposite = this
        }
    }

    private fun resolveDefaultColumn() {
        if (storage === null &&
            _mappedBy === null &&
            !_isTransient &&
            !isList &&
            !isConnection
        ) {
            setStorage(Column())
        }
    }

    private fun resolveTargetTypeBackProps() {
        _targetType?.let {
            (it as EntityTypeImpl).mutableBackProps += this
        }
    }

    protected open fun onInitialize() {}

    override fun toString(): String =
        kotlinProp.toString()
}