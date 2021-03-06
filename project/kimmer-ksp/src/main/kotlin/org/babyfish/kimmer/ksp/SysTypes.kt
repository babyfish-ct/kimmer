package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType

open class SysTypes internal constructor(
    val immutableType: KSType,
    resolver: Resolver
) {
    val draftType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_PACKAGE.Draft")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    val connectionType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_PACKAGE.graphql.Connection")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    val inputType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_PACKAGE.graphql.Input")
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

    val entityType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_PACKAGE.sql.Entity")
        ?.asStarProjectedType()
        ?: error("Internal bug")

    companion object {
        fun of(resolver: Resolver, table: Boolean): SysTypes? {
            val immutableType = resolver
                .getClassDeclarationByName("$KIMMER_PACKAGE.Immutable")
                ?.asStarProjectedType()
                ?: return null
            if (table) {
                return TableSysTypes(immutableType, resolver)
            }
            return SysTypes(immutableType, resolver)
        }
    }
}