package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStreamWriter

class DraftGenerator(
    private val codeGenerator: CodeGenerator,
    private val sysTypes: SysTypes,
    private val file: KSFile,
    private val modelClassDeclarations: List<KSClassDeclaration>
) {
    private val entityIDTypeNameProvider = EntityIDTypeNameProvider()

    fun generate(files: List<KSFile>) {
        val draftFileName =
            file.fileName.let {
                var lastDotIndex = it.lastIndexOf('.')
                if (lastDotIndex === -1) {
                    "${it}$DRAFT_SUFFIX"
                } else {
                    "${it.substring(0, lastDotIndex)}$DRAFT_SUFFIX"
                }
            }
        codeGenerator.createNewFile(
            Dependencies(false, *files.toTypedArray()),
            file.packageName.asString(),
            draftFileName
        ).use {
            val fileSpec = FileSpec
                .builder(
                    file.packageName.asString(),
                    draftFileName
                ).apply {
                    addAnnotation(
                        AnnotationSpec
                            .builder(Suppress::class)
                            .apply {
                                addMember("\"RedundantVisibilityModifier\"")
                                addMember("\"Unused\"")
                            }
                            .build()
                    )
                    for (classDeclaration in modelClassDeclarations) {
                        addType(classDeclaration)
                        if (!classDeclaration.isImmutableAbstract) {
                            addNewFun(classDeclaration, isAsync = false, forDraft = false)
                            addNewFun(classDeclaration, isAsync = true, forDraft = false)
                            addNewFun(classDeclaration, isAsync = false, forDraft = true)
                            addNewFun(classDeclaration, isAsync = true, forDraft = true)
                            addAddFun(classDeclaration, isAsync = false)
                            addAddFun(classDeclaration, isAsync = true)
                        }
                    }
                }.build()
            val writer = OutputStreamWriter(it, Charsets.UTF_8)
            fileSpec.writeTo(writer)
            writer.flush()
        }
    }

    private fun FileSpec.Builder.addType(classDeclaration: KSClassDeclaration) {
        addType(
            TypeSpec.interfaceBuilder(
                "${classDeclaration.simpleName.asString()}${DRAFT_SUFFIX}"
            ).apply {
                addTypeVariable(
                    TypeVariableName("T", classDeclaration.asClassName())
                )
                addSuperinterface(classDeclaration.asClassName())
                for (superType in classDeclaration.superTypes) {
                    val st = superType.resolve()
                    if (sysTypes.immutableType.isAssignableFrom(st)) {
                        when {
                            st == sysTypes.immutableType ->
                                addSuperinterface(
                                    ClassName(KIMMER_PACKAGE, "Draft")
                                        .parameterizedBy(TypeVariableName("T"))
                                )
                            st.starProjection() == sysTypes.entityType ->
                                addSuperinterface(
                                    ClassName("$KIMMER_PACKAGE.sql", "EntityDraft")
                                        .parameterizedBy(
                                            TypeVariableName("T"),
                                            entityIDTypeNameProvider[classDeclaration]
                                        )
                                )
                            else ->
                                if (st.arguments.isNotEmpty()) {
                                    throw GeneratorException(
                                        "Illegal immutable type '${classDeclaration.qualifiedName!!.asString()}', " +
                                            "its super interface '${st.declaration.qualifiedName!!.asString()}' has type arguments"
                                    )
                                } else {
                                    addSuperinterface(
                                        (st.declaration as KSClassDeclaration).asClassName {
                                            "$it$DRAFT_SUFFIX"
                                        }.parameterizedBy(TypeVariableName("T"))
                                    )
                                }
                        }
                    }
                }
                for (prop in classDeclaration.getDeclaredProperties()) {
                    addMembers(prop)
                }
                if (!classDeclaration.isImmutableAbstract) {
                    addNestedTypes(classDeclaration)
                }
            }.build()
        )
    }

    private fun TypeSpec.Builder.addMembers(prop: KSPropertyDeclaration) {
        if (prop.isMutable) {
            throw GeneratorException("The property '${prop.qualifiedName!!.asString()}' cannot be mutable")
        }
        val meta = PropMeta.of(prop, sysTypes)

        addProperty(
            PropertySpec.builder(
                prop.simpleName.asString(),
                meta.returnType,
                KModifier.OVERRIDE
            ).apply {
                mutable(true)
            }.build()
        )

        if (meta.targetDeclaration !== null) {
            addFunction(
                FunSpec
                    .builder(prop.simpleName.asString())
                    .apply {
                        modifiers += KModifier.ABSTRACT
                        returns(meta.draftFunReturnType)
                    }
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.addNestedTypes(declaration: KSClassDeclaration) {
        for (prefix in finalDraftPrefixes) {
            addType(
                TypeSpec
                    .interfaceBuilder(prefix)
                    .apply {
                        addSuperinterface(
                            declaration.asClassName {
                                "$it$DRAFT_SUFFIX"
                            }.parameterizedBy(
                                declaration.asClassName()
                            )
                        )
                        addSuperinterface(
                            ClassName(KIMMER_PACKAGE, "${prefix}Draft")
                                .parameterizedBy(
                                    declaration.asClassName()
                                )
                        )
                    }
                    .build()
            )
        }
    }

    private fun FileSpec.Builder.addNewFun(
        declaration: KSClassDeclaration,
        isAsync: Boolean,
        forDraft: Boolean
    ) {
        val mode = if (isAsync) "Async" else "Sync"
        val draftName = if (forDraft) "Draft" else ""
        addFunction(
            FunSpec
                .builder("by")
                .apply {
                    if (isAsync) {
                        modifiers += KModifier.SUSPEND
                    }
                    modifiers += KModifier.INLINE
                    receiver(
                        ClassName(KIMMER_PACKAGE, "${mode}${draftName}Creator")
                            .parameterizedBy(
                                declaration.asClassName()
                            )
                    )
                    addParameter(
                        ParameterSpec
                            .builder(
                                "base",
                                declaration.asClassName().copy(nullable = true)
                            )
                            .apply {
                                defaultValue("null")
                            }
                            .build()
                    )
                    addParameter(
                        "block",
                        LambdaTypeName.get(
                            declaration
                                .asClassName { "${it}Draft.$mode" },
                            emptyList(),
                            ClassName("kotlin", "Unit")
                        ).copy(suspending = isAsync),
                        KModifier.NOINLINE
                    )
                    if (forDraft) {
                        returns(
                            ClassName(
                                declaration.packageName.asString(),
                                "${declaration.simpleName.asString()}Draft",
                                mode
                            )
                        )
                    } else {
                        returns(declaration.asClassName())
                    }
                    val simpleName = declaration.simpleName.asString()
                    val suffix = if (isAsync) "Async" else ""
                    if (forDraft) {
                        addCode(
                            "return %T(type, base, block)",
                            ClassName(KIMMER_PACKAGE, "produceDraft$suffix")
                        )
                    } else {
                        addCode(
                            "return %T(type, base) { (this as ${simpleName}Draft.$mode).block() }",
                            ClassName(KIMMER_PACKAGE, "produce$suffix")
                        )
                    }
                }
                .build()
        )
    }

    private fun FileSpec.Builder.addAddFun(
        declaration: KSClassDeclaration,
        isAsync: Boolean
    ) {
        val mode = if (isAsync) "Async" else "Sync"
        val suffix = if (isAsync) "Async" else ""
        addFunction(
            FunSpec
                .builder("by")
                .apply {
                    modifiers += KModifier.INLINE
                    if (isAsync) {
                        modifiers += KModifier.SUSPEND
                    }
                    receiver(
                        ClassName(KIMMER_PACKAGE, "DraftList${mode}Adder")
                            .parameterizedBy(
                                declaration
                                    .asClassName { "$it$DRAFT_SUFFIX" }
                                    .parameterizedBy(
                                        WildcardTypeName.producerOf(
                                            declaration.asClassName()
                                        )
                                    )
                            )
                    )
                    addParameter(
                        ParameterSpec
                            .builder(
                                "base",
                                declaration.asClassName().copy(nullable = true)
                            )
                            .apply {
                                defaultValue("null")
                            }
                            .build()
                    )
                    addParameter(
                        "block",
                        LambdaTypeName.get(
                            declaration
                                .asClassName { "${it}Draft.$mode" },
                            emptyList(),
                            ClassName("kotlin", "Unit")
                        ).copy(suspending = isAsync),
                        KModifier.NOINLINE
                    )
                    addCode(
                        "list.add(%T(%T::class, base, block))",
                        ClassName(KIMMER_PACKAGE, "produceDraft$suffix"),
                        declaration.asClassName()
                    )
                }
                .build()
        )
    }
}

private val finalDraftPrefixes = listOf("Sync", "Async")