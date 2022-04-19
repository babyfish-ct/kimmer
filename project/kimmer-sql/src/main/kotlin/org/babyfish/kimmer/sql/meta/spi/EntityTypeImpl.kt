package org.babyfish.kimmer.sql.meta.spi

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.AssociationType
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.config.UserIdGenerator
import org.babyfish.kimmer.sql.meta.impl.EntityMappingBuilderImpl
import org.babyfish.kimmer.sql.meta.impl.ResolvingPhase
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

open class EntityTypeImpl(
    private val metaFactory: MetaFactory,
    override val immutableType: ImmutableType
): EntityType {

    init {
        if (immutableType is AssociationType) {
            throw IllegalArgumentException(
                "Cannot create entity type for association type '${immutableType}'"
            )
        }
        if (!Entity::class.java.isAssignableFrom(immutableType.kotlinType.java)) {
            throw IllegalArgumentException(
                "Cannot create entity type of '${immutableType.kotlinType.qualifiedName}' " +
                    "because it does inherit '${Entity::class.qualifiedName}'"
            )
        }
    }

    private var _superType: EntityTypeImpl? = null

    private val _derivedTypes = mutableListOf<EntityTypeImpl>()

    internal val mutableDeclaredProps = sortedMapOf<String, EntityPropImpl>()

    private var _props: Map<String, EntityProp>? = null

    private var _idProp: EntityProp? = null

    private var _idGenerator: IdGenerator? = null

    private var _versionProp: Any? = null // String | EntityProp

    private var _tableName: String? = null

    internal val mutableBackProps = mutableSetOf<EntityProp>()

    private var _expectedPhase = ResolvingPhase.SUPER_TYPE.ordinal

    override val tableName: String
        get() = _tableName ?: databaseIdentifier(kotlinType.simpleName!!)

    internal fun setTableName(tableName: String) {
        if (_tableName !== null) {
            throw MappingException(
                "Cannot set the table name of ${kotlinType.qualifiedName} " +
                    "because it has already been set"
            )
        }
        _tableName = tableName
    }

    internal fun setIdGenerator(idGenerator: IdGenerator) {
        _idGenerator = idGenerator
    }

    internal fun setVersionPropName(versionPropName: String) {
        _versionProp = versionPropName
    }

    override val superType: EntityType?
        get() = _superType

    override val derivedTypes: List<EntityType>
        get() = _derivedTypes

    override val idProp: EntityProp
        get() = _idProp ?: error("Id property has not been resolved")

    override val idGenerator: IdGenerator?
        get() = _idGenerator

    override val versionProp: EntityProp?
        get() = _versionProp?.let {
            it as? EntityProp ?: error("Version property has not been resolved")
        }

    override val declaredProps: Map<String, EntityProp>
        get() = mutableDeclaredProps

    override val props: Map<String, EntityProp>
        get() = _props ?: error("Properties have not been resolved")

    override val starProps: Map<String, EntityProp> by lazy {
        // select * from table
        mutableMapOf(idProp.name to idProp).also { map -> // mutableMapOf() is ordered, so id is first column
            props
                .values
                .filter {
                    // Ignore middle table because there are expensive
                    !it.isId && (it.storage is Column || it.storage is Formula<*, *, *>)
                }
                .associateByTo(map) { it.name }
        }
    }

    override val backProps: Set<EntityProp>
        get() = mutableBackProps

    internal fun resolve(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        if (shouldResolve(phase)) {
            when (phase) {
                ResolvingPhase.SUPER_TYPE -> resolveSuperTypes(builder)
                ResolvingPhase.DECLARED_PROPS -> resolveDeclaredProps(builder)
                ResolvingPhase.PROPS -> resolveProps(builder)
                ResolvingPhase.ID_PROP -> resolveIdProp()
                ResolvingPhase.VERSION_PROP -> resoleVersionProp()
                ResolvingPhase.ON_INITIALIZE_SPI -> onInitialize()
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
                mutableDeclaredProps[immutableProp.name] = metaFactory.createEntityProp(
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
            _props = map
        }
    }

    private fun resolvePropDetail(builder: EntityMappingBuilderImpl, phase: ResolvingPhase) {
        for (declaredProp in mutableDeclaredProps.values) {
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

    private fun resoleVersionProp() {
        val versionPropName = _versionProp as? String ?: return
        val prop = declaredProps[versionPropName]
            ?: MappingException(
                "Illegal version property '$versionPropName', " +
                    "There is no such declared property in '${immutableType.qualifiedName}'"
            )
    }

    protected open fun onInitialize() {}

    override fun toString(): String =
        immutableType.qualifiedName
}
