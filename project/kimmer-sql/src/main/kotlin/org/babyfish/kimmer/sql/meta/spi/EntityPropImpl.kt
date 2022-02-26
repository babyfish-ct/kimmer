package org.babyfish.kimmer.sql.meta.spi

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.meta.impl.ResolvingPhase
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class EntityPropImpl(
    override val declaringType: EntityType,
    kotlinProp: KProperty1<*, *>
): EntityProp {

    private var _mappedBy: Any? = null

    private var _opposite: EntityProp? = null

    private var _scalarProvider: ScalarProvider<*, *>? = null

    private var _storage: Storage? = null

    private var _targetType: EntityType? = null

    override val immutableProp: ImmutableProp =
        declaringType.immutableType.props[kotlinProp.name]
            ?: throw IllegalArgumentException("No prop '${kotlinProp.name}' of type '${declaringType.kotlinType.qualifiedName}'")

    override val isId: Boolean = kotlinProp.name == Entity<*>::id.name

    override val targetType: EntityType?
        get() = _targetType

    override val scalarProvider: ScalarProvider<*, *>?
        get() = _scalarProvider

    override val storage: Storage?
        get() = _storage

    override val returnType: KClass<*>
        get() = immutableProp.returnType

    override val isReference: Boolean
        get() = immutableProp.isReference

    override val isList: Boolean
        get() = immutableProp.isList

    override val isConnection: Boolean
        get() = immutableProp.isConnection

    override val isNullable: Boolean
        get() = immutableProp.isNullable

    override val isTargetNullable: Boolean
        get() = immutableProp.isTargetNullable

    override val mappedBy: EntityProp?
        get() = _mappedBy as EntityProp?

    override val opposite: EntityProp?
        get() = _opposite

    internal fun setMappedByName(name: String) {
        if (_mappedBy !== null) {
            throw MappingException(
                "Cannot set mappedBy of '${kotlinProp}' " +
                    "because it has already been set"
            )
        }
        _mappedBy = name
    }

    internal fun setStorage(storage: Storage) {
        if (_mappedBy !== null) {
            throw MappingException("Cannot configure storage for the inverse association '$kotlinProp'")
        }
        if ((isList || isConnection) && storage !is MiddleTable) {
            throw MappingException("list/connection association '$kotlinProp' only accept middle table")
        }
        if (isReference && storage is Formula<*, *, *>) {
            throw MappingException("reference association '$kotlinProp' does not accept formula")
        }
        _storage = storage
    }

    internal fun resolve(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.PROP_SCALAR_PROVIDER -> resolveScalarProvider(builder)
            ResolvingPhase.PROP_TARGET -> resolveTarget(builder)
            ResolvingPhase.PROP_MAPPED_BY -> resolvedMappedBy(builder)
            ResolvingPhase.PROP_DEFAULT_COLUMN -> resolveDefaultColumn()
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
            !isList &&
            !isConnection
        ) {
            setStorage(
                if (isReference) {
                    Column(databaseIdentifier("${name}_id"))
                } else {
                    Column(databaseIdentifier(name))
                }
            )
        }
    }

    protected open fun onInitialize() {}

    override fun toString(): String =
        kotlinProp.toString()
}