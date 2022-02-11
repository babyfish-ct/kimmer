package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName

class EntityIDTypeNameProvider {

    private val cache = mutableMapOf<String, ClassName>()

    operator fun get(declaration: KSClassDeclaration): ClassName =
        cache.computeIfAbsent(declaration.qualifiedName!!.asString()) {
            create(declaration)
        }

    private fun create(declaration: KSClassDeclaration): ClassName =
        findIdTypeName(declaration.asStarProjectedType())
            ?: throw GeneratorException(
                "Cannot resolve entity id type for '${declaration.qualifiedName!!.asString()}'"
            )

    private fun findIdTypeName(type: KSType): ClassName? {
        if (type.declaration.qualifiedName?.asString() == ENTITY_QUALIFIED_NAME) {
            val idTypeDeclaration = type.arguments[0].type?.resolve()?.declaration as? KSClassDeclaration
            if (idTypeDeclaration !== null) {
                return idTypeDeclaration.asClassName()
            }
        }
        for (superType in (type.declaration as KSClassDeclaration).superTypes) {
            val idTypeName = findIdTypeName(superType.resolve())
            if (idTypeName !== null) {
                return idTypeName
            }
        }
        return null
    }
}

private const val ENTITY_QUALIFIED_NAME = "$KIMMER_PACKAGE.sql.Entity"