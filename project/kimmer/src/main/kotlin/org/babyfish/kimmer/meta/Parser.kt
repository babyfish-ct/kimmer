package org.babyfish.kimmer.meta

import org.babyfish.kimmer.*
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.ConnectionDraft
import org.babyfish.kimmer.sql.Entity
import java.util.*
import kotlin.reflect.*

internal class Parser internal constructor(
    private val map: MutableMap<Class<*>, TypeImpl>
) {
    private var tmpMap = mutableMapOf<Class<*>, TypeImpl>()

    fun get(type: Class<out Immutable>): TypeImpl =
        map[type]
            ?: tmpMap[type]
            ?: create(type).also {
                tmpMap[type] = it
            }

    private fun create(type: Class<out Immutable>): TypeImpl {
        if (!type.isInterface) {
            throw IllegalArgumentException("type '${type.name}' is not interface")
        }
        if (!Immutable::class.java.isAssignableFrom(type)) {
            throw IllegalArgumentException("type '${type.name}' does not inherit '${Immutable::class.qualifiedName}'")
        }
        if (Draft::class.java.isAssignableFrom(type)) {
            throw IllegalArgumentException("type '${type.name}' cannot inherit '${Draft::class.qualifiedName}'")
        }
        if (ImmutableType::class.java === type) {
            throw IllegalArgumentException("type cannot be built-in type '${type.name}'")
        }
        val result = TypeImpl(type.kotlin)
        tmpMap[type] = result
        result.resolve(this)
        return result
    }

    fun terminate(): Map<Class<*>, TypeImpl> {
        val secondaryResolvedTypes = mutableSetOf<TypeImpl>()
        var size = 0
        while (tmpMap.size > size) {
            size = tmpMap.size
            val m = tmpMap.toMap()
            for (type in m.values) {
                if (secondaryResolvedTypes.add(type)) {
                    type.secondaryResolve(this)
                }
            }
        }
        return tmpMap
    }
}

internal class TypeImpl(
    override val kotlinType: KClass<out Immutable>
): ImmutableType {

    init {
        if (kotlinType.typeParameters.isNotEmpty()) {
            throw MetadataException("Type parameter is not allowed to immutable type '${kotlinType.qualifiedName}'")
        }
    }

    private val _superTypes = mutableSetOf<TypeImpl>()

    private val _declaredProps = mutableMapOf<String, PropImpl>()

    private var _props: Map<String, PropImpl>? = null

    override val isAbstract = kotlinType.java.isAnnotationPresent(Abstract::class.java)

    override val superTypes: Set<ImmutableType>
        get() = _superTypes

    override val declaredProps: Map<String, ImmutableProp>
        get() = _declaredProps

    override val props: Map<String, ImmutableProp>
        get() = _props ?: error("Internal bug")

    fun resolve(parser: Parser) {
        if (_props === null) {
            for (supertype in kotlinType.supertypes) {
                this.resolveSuper(supertype, parser)
            }
            for (superType in _superTypes) {
                superType.resolve(parser)
            }
            resolveDeclaredProps()
            resolveProps()
        }
    }

    private fun resolveSuper(superType: KType, parser: Parser) {
        val classifier = superType.classifier
        if (classifier !is KClass<*>) {
            error("Internal bug: classifier of super interface must be KClass")
        }
        if (classifier.java.isInterface &&
            classifier.java !== Immutable::class.java &&
            classifier.java !== Entity::class.java
        ) {
            val superType = parser.get(classifier.java as Class<out Immutable>)
            _superTypes += superType
        }
    }

    private fun resolveDeclaredProps() {
        for (member in kotlinType.members) {
            if (member.parameters.isNotEmpty() && member.parameters[0].kind == KParameter.Kind.INSTANCE) {
                if (member is KMutableProperty) {
                    throw MetadataException("'Illegal setter '${member.name}' in type ${kotlinType.qualifiedName}', setter is not allowed in immutable type")
                }
                if (member is KProperty1<*, *> && !isSuperProp(member)) {
                    _declaredProps[member.name] = PropImpl(this, member)
                }
            }
        }
    }

    private fun isSuperProp(kotlinProp: KProperty<*>): Boolean {
        var result = false
        for (superType in _superTypes) {
            val superProp = superType.props[kotlinProp.name]
            if (superProp !== null) {
                if (superProp.kotlinProp.returnType != kotlinProp.returnType) {
                    throw MetadataException("Prop '${kotlinProp}' overrides '${superProp.kotlinProp}' but changes the return type")
                }
                result = true
            }
        }
        return result
    }

    private fun resolveProps() {
        _props = if (this.superTypes.isEmpty()) {
            _declaredProps
        } else {
            val props = _declaredProps.toMutableMap()
            for (superType in _superTypes) {
                for (superProp in superType._props!!.values) {
                    props.putIfAbsent(superProp.kotlinProp.name, superProp)
                }
            }
            props
        }
    }

    fun secondaryResolve(parser: Parser) {
        for (declaredProp in _declaredProps.values) {
            declaredProp.resolve(parser)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override val draftInfo: DraftInfo? by lazy {
        if (kotlinType == Connection.PageInfo::class) {
            DraftInfo(
                ConnectionDraft.PageInfoDraft::class.java,
                ConnectionDraft.PageInfoDraft.Sync::class.java,
                ConnectionDraft.PageInfoDraft.Async::class.java
            )
        } else {
            getAbstractDraftType(this)?.let {
                val syncDraftType =
                    getFinalDraftType(it, SyncDraft::class.java) as Class<out SyncDraft<*>>?
                val asyncDraftType =
                    getFinalDraftType(it, AsyncDraft::class.java) as Class<out AsyncDraft<*>>?
                if (!isAbstract) {
                    syncDraftType
                        ?: error("No nested interface 'sync' for non-abstract immutable type '${it.name}'")
                    asyncDraftType
                        ?: error("No nested interface 'async' for non-abstract immutable type '${it.name}'")
                }
                DraftInfo(
                    it,
                    syncDraftType,
                    asyncDraftType
                )
            }
        }
    }

    override fun toString(): String =
        kotlinType.qualifiedName!!
}

private class PropImpl(
    override val declaringType: ImmutableType,
    override val kotlinProp: KProperty1<*, *>
): ImmutableProp {

    override val returnType: KClass<*> =
        kotlinProp.returnType.classifier as? KClass<*>
            ?: error("Internal bug: '${kotlinProp}' does not returns class")

    override val javaReturnType: Class<*> =
        returnType.let {
            when {
                isNullable ->
                    it.javaObjectType
                name == "id" &&
                    Entity::class.java.isAssignableFrom(declaringType.kotlinType.java) ->
                    it.javaObjectType
                else ->
                    it.java
            }
        }

    override val isConnection: Boolean

    override val isList: Boolean

    override val elementType: KClass<*>

    override val isReference: Boolean

    override val isScalarList: Boolean

    override val isElementNullable: Boolean

    private var _targetType: ImmutableType? = null

    init {

        if (Map::class.java.isAssignableFrom(javaReturnType)) {
            throw MetadataException("Illegal property '${kotlinProp}', map is not allowed")
        }
        if (javaReturnType.isArray) {
            throw MetadataException("Illegal property '${kotlinProp}', array is not allowed")
        }
        isConnection = Connection::class.java.isAssignableFrom(javaReturnType)
        var isCollection = Collection::class.java.isAssignableFrom(javaReturnType)
        isReference = !isConnection && Immutable::class.java.isAssignableFrom(javaReturnType)
        if (isConnection && isCollection) {
            throw MetadataException(
                "Illegal property '${kotlinProp}', its return type cannot be both connection and collection"
            )
        }
        elementType = when {
            isConnection -> {
                if (returnType != Connection::class) {
                    throw MetadataException(
                        "Illegal property '${kotlinProp}', the type of connection " +
                            "must must be strictly equal to '${Connection::class.qualifiedName}'"
                    )
                }
                val targetClassifier = kotlinProp.returnType.arguments[0].type?.classifier
                if (targetClassifier !is KClass<*>) {
                    throw MetadataException("Illegal property '${kotlinProp}', generic argument of connection is not class")
                }
                targetClassifier
            }
            isCollection -> {
                if (returnType != List::class) {
                    throw MetadataException(
                        "Illegal property '${kotlinProp}', the type of list " +
                            "must must be strictly equal to '${List::class.qualifiedName}'"
                    )
                }
                val targetClassifier = kotlinProp.returnType.arguments[0].type?.classifier
                if (targetClassifier !is KClass<*>) {
                    throw MetadataException("Illegal property '${kotlinProp}', generic argument of list is not class")
                }
                targetClassifier
            }
            else ->
                returnType
        }
        isList = isCollection && Immutable::class.java.isAssignableFrom(elementType.java)
        isScalarList = isCollection && !isList
        if (isScalarList && Entity::class.java.isAssignableFrom(declaringType.kotlinType.java)) {
            throw MetadataException("Illegal property '${kotlinProp}', scalar list is not allowed for Entity interface")
        }
        isElementNullable = if (isConnection || isCollection) {
            kotlinProp.returnType.arguments[0].type?.isMarkedNullable ?: false
        } else {
            false
        }
        if (isConnection) {
            throw MetadataException("Current version temporarily does not support connection prop '$kotlinProp'")
        }
        if (isScalarList) {
            throw MetadataException("Current version temporarily does not support scalar list prop '$kotlinProp'")
        }
    }

    override val isNullable: Boolean
        get() = kotlinProp.returnType.isMarkedNullable

    override val isAssociation: Boolean
        get() = isConnection || isList || isReference

    override val targetType: ImmutableType?
        get() = _targetType

    @Suppress("UNCHECKED_CAST")
    fun resolve(parser: Parser) {
        if (isAssociation) {
            val expectedType =
                if (Entity::class.java.isAssignableFrom(declaringType.kotlinType.java)) {
                    Entity::class.java
                } else {
                    Immutable::class.java
                }
            if (!expectedType.isAssignableFrom(elementType.java)) {
                throw MetadataException(
                    "Illegal association property '${kotlinProp}', its target type " +
                        "'${elementType.qualifiedName}' is not derived type of '${expectedType.name}'"
                )
            }
            _targetType = parser.get(elementType.java as Class<out Immutable>)
        }
    }

    override fun toString(): String =
        kotlinProp.toString()
}

private fun getAbstractDraftType(immutableType: ImmutableType): Class<Draft<*>>? {
    val javaType = immutableType.kotlinType.java
    val abstractDraftType =
        try {
            Class.forName("${javaType.name}Draft")
        } catch (ex: ClassNotFoundException) {
            return null
        }
    if (!abstractDraftType.isInterface) {
        throw MetadataException(
            "As the draft interface type of '${javaType.name}', " +
                "'${abstractDraftType.name}' must be interface"
        )
    }
    if (!javaType.isAssignableFrom(abstractDraftType)) {
        throw MetadataException(
            "As the draft interface type of '${javaType.name}', " +
                "'${abstractDraftType.name}' must extend '${javaType.name}'"
        )
    }
    if (!Draft::class.java.isAssignableFrom(abstractDraftType)) {
        throw MetadataException(
            "As the draft interface type of '${javaType.name}', " +
                "'${abstractDraftType.name}' must extend '${Draft::class.java.name}'"
        )
    }
    if (SyncDraft::class.java.isAssignableFrom(abstractDraftType)) {
        throw MetadataException(
            "As the abstract draft interface type of '${javaType.name}', " +
                "'${abstractDraftType.name}' cannot extend '${SyncDraft::class.java.name}'"
        )
    }
    if (AsyncDraft::class.java.isAssignableFrom(abstractDraftType)) {
        throw MetadataException(
            "As the abstract draft interface type of '${javaType.name}', " +
                "'${abstractDraftType.name}' cannot extend '${AsyncDraft::class.java.name}'"
        )
    }
    if (abstractDraftType.typeParameters.size != 1 ||
        !Arrays.equals(abstractDraftType.typeParameters[0].bounds, arrayOf(javaType))) {
        throw MetadataException(
            "As the abstract draft interface type of '${javaType.name}', " +
                "'${abstractDraftType.name}' must have one type argument whose bound is '${javaType.name}'"
        )
    }
    return abstractDraftType as Class<Draft<*>>
}

private fun getFinalDraftType(
    abstractDraftType: Class<*>,
    draftContractType: Class<*>
): Class<*> ? {
    val simpleName = when (draftContractType) {
        SyncDraft::class.java -> "Sync"
        AsyncDraft::class.java -> "Async"
        else -> error("Internal bug")
    }
    val finalDraftType = abstractDraftType.declaredClasses.find {
        it.simpleName == simpleName
    } ?: return null

    if (!finalDraftType.isInterface) {
        throw MetadataException("'${finalDraftType.name}' must be interface")
    }
    if (!draftContractType.isAssignableFrom(finalDraftType)) {
        throw MetadataException("'${finalDraftType.name}' must extend '${draftContractType.name}'")
    }
    if (finalDraftType.typeParameters.isNotEmpty()) {
        throw MetadataException("'${finalDraftType.name}' cannot have type parameters")
    }
    return finalDraftType
}