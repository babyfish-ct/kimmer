package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import java.lang.IllegalStateException

class SysTypes private constructor(
    val immutableType: KSType,
    resolver: Resolver
) {
    val draftType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_PACKAGE.Draft")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    val connectionType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_PACKAGE.Connection")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    val collectionType: KSType = resolver
        .getClassDeclarationByName("kotlin.collections.Collection")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    val listType: KSType = resolver
        .getClassDeclarationByName("kotlin.collections.List")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    val mapType: KSType = resolver
        .getClassDeclarationByName("kotlin.collections.Map")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    companion object {
        fun of(resolver: Resolver): SysTypes? {
            val immutableType = resolver
                .getClassDeclarationByName("$KIMMER_PACKAGE.Immutable")
                ?.asStarProjectedType()
                ?: return null
            return SysTypes(immutableType, resolver)
        }
    }
}