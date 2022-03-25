package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import java.sql.Connection

data class PropMeta(
    val prop: KSPropertyDeclaration,
    val isNullable: Boolean,
    val targetDeclaration: KSClassDeclaration? = null,
    val isConnection: Boolean = false,
    val isList: Boolean = false,
    val isReference: Boolean = false,
    val isScalarList: Boolean = false,
    val isElementNullable: Boolean = false
) {
    val returnType: TypeName by lazy {
        when {
            isConnection ->
                ClassName("$KIMMER_PACKAGE.graphql", "Connection")
                    .parameterizedBy(targetDeclaration!!.asClassName())
            isList || isScalarList ->
                ClassName("kotlin.collections", "List")
                    .parameterizedBy(
                        targetDeclaration!!.asClassName()
                    )
            else ->
                scalarTypeName
        }
    }

    val draftFunReturnType: TypeName by lazy {
        targetDeclaration
            ?.run {
                if (isScalarList) {
                    ClassName("kotlin.collections", "MutableList")
                        .parameterizedBy(asClassName())
                } else {
                    val draftTypeName = asClassName {
                        "$it$DRAFT_SUFFIX"
                    }.parameterizedBy(
                        WildcardTypeName.producerOf(asClassName())
                    )
                    when {
                        isConnection ->
                            ClassName("$KIMMER_PACKAGE.graphql", "Connection")
                                .parameterizedBy(targetDeclaration!!.asClassName())
                        isList ->
                            ClassName("kotlin.collections", "MutableList")
                                .parameterizedBy(draftTypeName)
                        else ->
                            draftTypeName
                    }
                }
            }
            ?: scalarTypeName
    }

    private val scalarTypeName: TypeName
        get() = (
            prop.type.resolve().declaration as? KSClassDeclaration
                ?: throw GeneratorException("The property '${prop}' must returns class/interface type")
            ).asClassName().copy(nullable = isElementNullable)

    companion object {
        fun of(prop: KSPropertyDeclaration, sysTypes: SysTypes, mustBeEntity: Boolean = false): PropMeta {
            val nullableType = prop.type.resolve()
            val nonNullType = nullableType.makeNotNullable()
            if (sysTypes.mapType.isAssignableFrom(nonNullType)) {
                throw GeneratorException("The property '${prop.qualifiedName!!.asString()}' cannot be map")
            }
            val isConnection = sysTypes.connectionType.isAssignableFrom(nonNullType)
            val mayBeCollection = sysTypes.collectionType.isAssignableFrom(nonNullType)
            if (isConnection && mayBeCollection) {
                throw GeneratorException("The property '${prop.qualifiedName!!.asString()}' cannot be both connection and collection")
            }
            val elementType = when {
                isConnection -> {
                    if (nonNullType.declaration != sysTypes.connectionType.declaration) {
                        throw GeneratorException(
                            "The property '${prop.qualifiedName!!.asString()}' " +
                                "must be exactly equal to '${Connection::class.qualifiedName}'"
                        )
                    }
                    nonNullType.arguments[0].type?.resolve() ?: throw GeneratorException(
                        "Cannot get connection node type from '${prop.qualifiedName!!.asString()}'"
                    )
                }
                mayBeCollection -> {
                    if (nonNullType.declaration != sysTypes.listType.declaration) {
                        throw GeneratorException(
                            "The property '${prop.qualifiedName!!.asString()}' " +
                                "must be exactly equal to 'kotlin.collections.List'"
                        )
                    }
                    nonNullType.arguments[0].type?.resolve()
                        ?: throw GeneratorException(
                            "Cannot get list element type from '${prop.qualifiedName!!.asString()}'"
                        )
                }
                sysTypes.immutableType.isAssignableFrom(nonNullType) -> {
                    nonNullType
                }
                else -> null
            } ?: return PropMeta(prop, nullableType.isMarkedNullable)

            val isCollection = mayBeCollection && sysTypes.immutableType.isAssignableFrom(elementType)
            val isScalarCollection = mayBeCollection && !isCollection
            val elementDeclaration = elementType.declaration as? KSClassDeclaration
                ?: throw GeneratorException(
                    "The property '${prop.qualifiedName!!.asString()}' is not valid property, " +
                        "its element type '${elementType.declaration.qualifiedName!!.asString()}' is not class "
                )
            if (!isScalarCollection) {
                val expectedTargetType =
                    if (mustBeEntity) {
                        (sysTypes as TableSysTypes).entityType
                    } else {
                        sysTypes.immutableType
                    }
                if (!expectedTargetType.isAssignableFrom(elementType) ||
                    elementDeclaration.classKind != ClassKind.INTERFACE
                ) {
                    throw GeneratorException(
                        "The property '${prop.qualifiedName!!.asString()}' is not valid association, " +
                            "its target type '${elementDeclaration.qualifiedName!!.asString()}' is not " +
                            "interface extends '${expectedTargetType.declaration.qualifiedName?.asString()}'"
                    )
                }
            }
            if (elementDeclaration.typeParameters.isNotEmpty()) {
                throw GeneratorException(
                    "The property '${prop.qualifiedName!!.asString()}' is not valid association, " +
                        "its target type '${elementDeclaration.qualifiedName!!.asString()}' cannot " +
                        "have type argument"
                )
            }
            return PropMeta(
                prop,
                nullableType.isMarkedNullable,
                elementDeclaration,
                isConnection = isConnection,
                isList = isCollection,
                isReference = !isConnection && !mayBeCollection,
                isScalarList = isScalarCollection,
                elementType.isMarkedNullable
            )
        }
    }
}