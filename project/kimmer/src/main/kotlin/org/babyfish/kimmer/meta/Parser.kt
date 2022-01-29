package org.babyfish.kimmer.meta

import org.babyfish.kimmer.*
import org.babyfish.kimmer.graphql.Connection
import org.springframework.core.GenericTypeResolver
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
        if (classifier.java.isInterface && classifier.java !== Immutable::class.java) {
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

    override val draftInfo: DraftInfo? by lazy {
        getAbstractDraftType(this)?.let {
            val syncDraftType =
                getFinalDraftType(it, SyncDraft::class.java) as Class<out SyncDraft<*>>?
            val asyncDraftType =
                getFinalDraftType(it, AsyncDraft::class.java) as Class<out AsyncDraft<*>>?
            if (!isAbstract) {
                syncDraftType
                    ?: error("No nested interface 'sync' for non-abstract immutable type '${it::class.qualifiedName}'")
                asyncDraftType
                    ?: error("No nested interface 'async' for non-abstract immutable type '${it::class.qualifiedName}'")
            }
            DraftInfo(
                it,
                syncDraftType,
                asyncDraftType
            )
        }
    }
}

private class PropImpl(
    override val declaringType: ImmutableType,
    override val kotlinProp: KProperty1<*, *>
): ImmutableProp {

    override val returnType: KClass<*> =
        kotlinProp.returnType.classifier as? KClass<*>
            ?: error("Internal bug: '${kotlinProp}' does not returns class")

    override val isConnection: Boolean

    override val isList: Boolean

    override val isReference: Boolean

    override val isTargetNullable: Boolean

    private var _targetType: ImmutableType? = null

    init {

        if (Map::class.java.isAssignableFrom(returnType.java)) {
            throw MetadataException("Illegal property '${kotlinProp}', map is not allowed")
        }
        if (returnType.java.isArray) {
            throw MetadataException("Illegal property '${kotlinProp}', array is not allowed")
        }
        isConnection = Connection::class.java.isAssignableFrom(returnType.java)
        isList = !isConnection && Collection::class.java.isAssignableFrom(returnType.java)
        isReference = !isConnection && !isList && Immutable::class.java.isAssignableFrom(returnType.java)
        isTargetNullable = if (isList) {
            kotlinProp.returnType.arguments[0].type?.isMarkedNullable ?: false
        } else {
            false
        }
    }

    override val isNullable: Boolean
        get() = kotlinProp.returnType.isMarkedNullable

    override val isAssociation: Boolean
        get() = isConnection || isList || isReference

    override val targetType: ImmutableType?
        get() = _targetType

    fun resolve(parser: Parser) {
        val cls = kotlinProp.returnType.classifier as KClass<*>
        if (isConnection) {
            if (cls.typeParameters.isNotEmpty()) {
                throw MetadataException("Illegal property '${kotlinProp}', connection property of immutable object must return derived type of '${Connection::class.qualifiedName}' without type parameters")
            }
            val targetJavaType = GenericTypeResolver.resolveTypeArgument(cls.java, Connection::class.java)
            _targetType = parser.get(targetJavaType as Class<out Immutable>)
        } else if (isList) {
            if (cls != List::class) {
                throw MetadataException("Illegal property '${kotlinProp}', list property of immutable object must return 'kotlin.List'")
            }
            val targetClassifier = kotlinProp.returnType.arguments[0].type?.classifier
            if (targetClassifier !is KClass<*> || !Immutable::class.java.isAssignableFrom(targetClassifier.java)) {
                throw MetadataException("Illegal property '${kotlinProp}', generic argument of list is not immutable type")
            }
            _targetType = parser.get(targetClassifier.java as Class<out Immutable>)
        } else if (isReference) {
            _targetType = parser.get(cls.java as Class<out Immutable>)
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