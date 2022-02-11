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
    val nullable: Boolean,
    val targetDeclaration: KSClassDeclaration? = null,
    val isConnection: Boolean = false,
    val isList: Boolean = false,
    val isReference: Boolean = false,
    val isTargetNullable: Boolean = false
) {
    val returnType: TypeName by lazy {
        if (isList) {
            ClassName("kotlin.collections", "List")
                .parameterizedBy(
                    targetDeclaration!!.asClassName()
                )
        } else {
            scalarTypeName
        }
    }

    val draftFunReturnType: TypeName by lazy {
        targetDeclaration
            ?.run {
                val draftTypeName = asClassName {
                    "$it$DRAFT_SUFFIX"
                }.parameterizedBy(
                    WildcardTypeName.producerOf(asClassName())
                )
                if (isList) {
                    ClassName("kotlin.collections", "MutableList")
                        .parameterizedBy(draftTypeName)
                } else {
                    draftTypeName
                }
            }
            ?: scalarTypeName
    }

    private val scalarTypeName: TypeName
        get() = (
            prop.type.resolve().declaration as? KSClassDeclaration
                ?: throw GeneratorException("The property '${prop}' must returns class/interface type")
            ).asClassName().copy(nullable = isTargetNullable)

    companion object {
        fun of(prop: KSPropertyDeclaration, sysTypes: SysTypes, mustBeEntity: Boolean = false): PropMeta {
            val nullableType = prop.type.resolve()
            val nonNullType = nullableType.makeNotNullable()
            if (sysTypes.mapType.isAssignableFrom(nonNullType)) {
                throw GeneratorException("The property '${prop.qualifiedName!!.asString()}' cannot be map")
            }
            val isConnection = sysTypes.connectionType.isAssignableFrom(nonNullType)
            val isCollection = sysTypes.collectionType.isAssignableFrom(nonNullType)
            if (isConnection && isCollection) {
                throw GeneratorException("The property '${prop.qualifiedName!!.asString()}' cannot be both connection and collection")
            }
            val targetType = when {
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
                isCollection -> {
                    if (nonNullType.declaration != sysTypes.listType.declaration) {
                        throw GeneratorException(
                            "The property '${prop.qualifiedName!!.asString()}' " +
                                "must be exactly equal to 'kotlin.collections.List'"
                        )
                    }
                    nonNullType.arguments[0].type?.resolve() ?: throw GeneratorException(
                        "Cannot get list element type from '${prop.qualifiedName!!.asString()}'"
                    )
                }
                sysTypes.immutableType.isAssignableFrom(nonNullType) -> {
                    nonNullType
                }
                else -> null
            } ?: return PropMeta(prop, nullableType.isMarkedNullable)

            val targetDeclaration = targetType.declaration
            val expectedTargetType = if (mustBeEntity) {
                (sysTypes as TableSysTypes).entityType
            } else {
                sysTypes.immutableType
            }
            if (!expectedTargetType.isAssignableFrom(targetType) ||
                targetDeclaration !is KSClassDeclaration ||
                targetDeclaration.classKind != ClassKind.INTERFACE
            ) {
                throw GeneratorException(
                    "The property '${prop.qualifiedName!!.asString()}' is not valid association, " +
                        "its target type '${targetDeclaration.qualifiedName!!.asString()}' is not " +
                        "interface extends '${expectedTargetType.declaration.qualifiedName?.asString()}'"
                )
            }
            if (targetDeclaration.typeParameters.isNotEmpty()) {
                throw GeneratorException(
                    "The property '${prop.qualifiedName!!.asString()}' is not valid association, " +
                        "its target type '${targetDeclaration.qualifiedName!!.asString()}' cannot " +
                        "have type argument"
                )
            }
            if (sysTypes.inputType.isAssignableFrom(targetType)) {
                throw GeneratorException(
                    "The property '${prop.qualifiedName!!.asString()}' is not valid association, " +
                        "its target type '${targetDeclaration.qualifiedName!!.asString()}' cannot " +
                        "be interface extends Input"
                )
            }
            return PropMeta(
                prop,
                nullableType.isMarkedNullable,
                targetDeclaration,
                isConnection,
                isCollection,
                !isConnection && !isCollection,
                if (isCollection) {
                    targetType.isMarkedNullable
                } else {
                    !sysTypes.connectionType.isAssignableFrom(nonNullType) && nullableType.isMarkedNullable
                }
            )
        }
    }
}