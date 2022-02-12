package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.time.Duration

internal class EntityPropImpl(
    override val declaringType: EntityType,
    kotlinProp: KProperty1<*, *>
): EntityProp {

    private var _mappedBy: Any? = null

    private var _opposite: EntityProp? = null

    private var _storage: Storage? = null

    override val immutableProp: ImmutableProp =
        declaringType.immutableType.props[kotlinProp.name]
            ?: throw IllegalArgumentException("No prop '${kotlinProp.name}' of type '${declaringType.kotlinType.qualifiedName}'")

    override val isId: Boolean = kotlinProp.name == Entity<*>::id.name

    override var targetType: EntityType? = null

    override var oppositeProp: EntityProp? = null

    override var storage: Storage?
        get() = _storage
        set(value) {
            if (_mappedBy !== null) {
                throw MappingException("Cannot configure storage for the inverse association '$kotlinProp'")
            }
            if ((isList || isConnection) && value !is MiddleTable) {
                throw MappingException("list/connection association '$kotlinProp' only accept middle table")
            }
            if (isReference && value is Formula<*, *, *>) {
                throw MappingException("reference association '$kotlinProp' does not accept formula")
            }
            _storage = value
        }

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

    fun setMappedByName(name: String) {
        if (_mappedBy !== null) {
            throw MappingException(
                "Cannot set mappedBy of '${kotlinProp}' " +
                    "because it has already been set"
            )
        }
        _mappedBy = name
    }

    fun resolve(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.PROP_TARGET -> resolveTarget(builder)
            ResolvingPhase.PROP_MAPPED_BY -> resolvedMappedBy(builder)
            ResolvingPhase.PROP_DEFAULT_COLUMN -> resolveDefaultColumn()
        }
    }

    private fun resolveTarget(builder: EntityMappingBuilderImpl) {
        immutableProp.targetType?.let {
            val tgtType = builder[it]
            targetType = tgtType
        }
    }

    private fun resolvedMappedBy(builder: EntityMappingBuilderImpl) {
        if (_mappedBy is String) {
            val mappedByProp = (targetType!!.props[_mappedBy] as EntityPropImpl?)
                ?: throw MappingException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}', " +
                        "but there is not property named '${_mappedBy}' in the target type '${targetType}' "
                )
            if (mappedByProp.targetType !== declaringType) {
                throw MappingException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${targetType}' is not " +
                        "association point to current type '${declaringType}'"
                )
            }
            if (mappedByProp._mappedBy !== null) {
                throw MappingException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${targetType}' is specified " +
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
            storage =
                if (isReference) {
                    Column(databaseIdentifier("${name}_id"))
                } else {
                    Column(databaseIdentifier(name))
                }
        }
    }


    override fun toString(): String =
        kotlinProp.toString()
}