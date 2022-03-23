package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*

class ImmutableProcessor(
    private val draft: Boolean,
    private val table: Boolean,
    private val tableCollectionJoinOnlyForSubQuery: Boolean,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true
        val sysTypes = SysTypes.of(resolver, table) ?: return mutableListOf()
        val modelMap = findModelMap(resolver, sysTypes)
        if (modelMap.isNotEmpty()) {
            for ((file, declarations) in modelMap) {
                if (draft) {
                    DraftGenerator(codeGenerator, sysTypes, file, declarations)
                        .generate(resolver.getAllFiles().toList())
                }
                if (table) {
                    val tableSysTypes = sysTypes as TableSysTypes
                    val entityDeclarations = declarations.filter {
                        tableSysTypes.entityType.isAssignableFrom(it.asStarProjectedType())
                    }
                    if (entityDeclarations.isNotEmpty()) {
                        if (entityDeclarations.size > 1) {
                            throw GeneratorException(
                                "When the ksp argument 'kimmer.table' is true, " +
                                    "each source file can only declare one entity interface inherits " +
                                    "'${tableSysTypes.entityType.declaration.qualifiedName!!.asString()}'. " +
                                    "However, the source file '${file.filePath}' declares ${entityDeclarations.size} entity types: " +
                                    entityDeclarations.joinToString { "'${it.simpleName!!.asString()}'" }
                            )
                        }
                        TableGenerator(codeGenerator, tableSysTypes, file, entityDeclarations, tableCollectionJoinOnlyForSubQuery)
                            .generate(resolver.getAllFiles().toList())
                    }
                }
            }
        }
        return emptyList()
    }

    private fun findModelMap(resolver: Resolver, sysTypes: SysTypes): Map<KSFile, List<KSClassDeclaration>> {

        val modelMap = mutableMapOf<KSFile, MutableList<KSClassDeclaration>>()
        for (file in resolver.getAllFiles()) {
            for (classDeclaration in file.declarations.filterIsInstance<KSClassDeclaration>()) {
                if (classDeclaration.classKind == ClassKind.INTERFACE &&
                    classDeclaration.typeParameters.isEmpty() &&
                    classDeclaration.qualifiedName !== null &&
                    sysTypes.immutableType.isAssignableFrom(classDeclaration.asStarProjectedType()) &&
                    !sysTypes.draftType.isAssignableFrom(classDeclaration.asStarProjectedType())
                ) {
                    if (classDeclaration.isPrivate() || classDeclaration.isProtected()) {
                        throw GeneratorException("The immutable interface '${classDeclaration.qualifiedName!!.asString()} cannot be private or protected'")
                    }
                    modelMap.computeIfAbsent(file) {
                        mutableListOf()
                    } += classDeclaration
                }
            }
        }
        return modelMap
    }
}
