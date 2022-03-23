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
                        val asyncValues = listOf(false, true)
                        val drafts = listOf(false, true)
                        val edges = listOf(false, true)
                        if (!classDeclaration.isImmutableAbstract) {
                            for (draft in drafts) {
                                for (async in asyncValues) {
                                    addNewFun(classDeclaration, async, draft)
                                }
                            }
                            for (edge in edges) {
                                for (async in asyncValues) {
                                    addAddFun(classDeclaration, async, edge)
                                }
                            }
                            addNodeFun(classDeclaration)
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
        val asyncMode = if (isAsync) "Async" else "Sync"
        val optionalAsyncName = if (isAsync) "Async" else ""
        val optionalDraftName = if (forDraft) "Draft" else ""
        val creatorClassName =
            ClassName(KIMMER_PACKAGE,"$asyncMode${optionalDraftName}Creator").parameterizedBy(
            declaration.asClassName()
        )
        val baseClassName = declaration.asClassName()
        val draftClassName = declaration.asClassNameByArray { arrayOf("${it}Draft", asyncMode) }
        val produceName = "produce$optionalDraftName$optionalAsyncName"
        addFunction(
            FunSpec
                .builder("by")
                .apply {
                    if (isAsync) {
                        modifiers += KModifier.SUSPEND
                    }
                    receiver(creatorClassName)
                    addParameter(
                        ParameterSpec
                            .builder(
                                "base",
                                baseClassName.copy(nullable = true)
                            )
                            .apply {
                                defaultValue("null")
                            }
                            .build()
                    )
                    addParameter(
                        "block",
                        LambdaTypeName.get(
                            draftClassName,
                            emptyList(),
                            ClassName("kotlin", "Unit")
                        ).copy(suspending = isAsync)
                    )
                    if (forDraft) {
                        returns(draftClassName)
                    } else {
                        returns(baseClassName)
                    }
                    val simpleName = declaration.simpleName.asString()
                    if (!forDraft) {
                        addCode(
                            "return %T(type, base) { (this as ${simpleName}Draft.$asyncMode).block() }",
                            ClassName(KIMMER_PACKAGE, produceName)
                        )
                    } else {
                        addCode(
                            "return %T(type, base, block)",
                            ClassName(KIMMER_PACKAGE, produceName)
                        )
                    }
                }
                .build()
        )
    }

    private fun FileSpec.Builder.addAddFun(
        declaration: KSClassDeclaration,
        isAsync: Boolean,
        forEdge: Boolean
    ) {
        val asyncMode = if (isAsync) "Async" else "Sync"
        val optionalAsyncName = if (isAsync) "Async" else ""
        val baseClassName = if (forEdge) {
            ClassName("$KIMMER_PACKAGE.graphql", "Connection", "Edge")
                .parameterizedBy(declaration.asClassName())
        } else {
            declaration.asClassName()
        }
        val produceClassName = if (forEdge) {
            ClassName("$KIMMER_PACKAGE.graphql", "produceEdgeDraft$optionalAsyncName")
        } else {
            ClassName(KIMMER_PACKAGE, "produceDraft$optionalAsyncName")
        }
        val receiverClassName = if (forEdge) {
            ClassName(KIMMER_PACKAGE, "${asyncMode}EdgeDraftListAdder")
                .parameterizedBy(declaration.asClassName())
        } else {
            ClassName(KIMMER_PACKAGE, "${asyncMode}DraftListAdder")
                .parameterizedBy(
                    declaration
                        .asClassName { "$it$DRAFT_SUFFIX" }
                        .parameterizedBy(
                            WildcardTypeName.producerOf(
                                declaration.asClassName()
                            )
                        )
                )
        }
        val draftClassName = if (forEdge) {
            ClassName(
                "$KIMMER_PACKAGE.graphql",
                "ConnectionDraft",
                "EdgeDraft",
                asyncMode
            ).parameterizedBy(declaration.asClassName())
        } else {
            declaration.asClassNameByArray { arrayOf("${it}Draft", asyncMode) }
        }
        addFunction(
            FunSpec
                .builder("by")
                .apply {
                    if (isAsync) {
                        modifiers += KModifier.SUSPEND
                    }
                    receiver(receiverClassName)
                    addParameter(
                        ParameterSpec
                            .builder(
                                "base",
                                baseClassName.copy(nullable = true)
                            )
                            .apply {
                                defaultValue("null")
                            }
                            .build()
                    )
                    addParameter(
                        "block",
                        LambdaTypeName.get(
                            draftClassName,
                            emptyList(),
                            ClassName("kotlin", "Unit")
                        ).copy(suspending = isAsync)
                    )
                    addCode(
                        "list.add(%T(%T::class, base, block))",
                        produceClassName,
                        declaration.asClassName()
                    )
                }
                .build()
        )
    }

    private fun FileSpec.Builder.addNodeFun(
        declaration: KSClassDeclaration
    ) {
        val untypedEdgeDraftClassName =
            ClassName("$KIMMER_PACKAGE.graphql", "ConnectionDraft", "EdgeDraft")
        val nodeDraftClassName =
            declaration
                .asClassName { "${it}Draft" }
                .parameterizedBy(
                    WildcardTypeName.producerOf(
                        declaration.asClassName()
                    )
                )
        val propFieldName = "${
            declaration.qualifiedName!!.asString().replace('.', '_')
        }_EdgeNodeProp"
        addFunction(
            FunSpec.builder("node")
                .apply {
                    receiver(
                        untypedEdgeDraftClassName
                            .parameterizedBy(
                                declaration.asClassName()
                            )
                    )
                    returns(nodeDraftClassName)
                    addCode(
                        "return %T.getOrCreate(this, $propFieldName) as %T",
                        ClassName(KIMMER_PACKAGE, "Draft"),
                        nodeDraftClassName
                    )
                }
                .build()
        )
        addProperty(
            PropertySpec.builder(
                propFieldName,
                ClassName("$KIMMER_PACKAGE.meta", "ImmutableProp")
            ).apply {
                modifiers += KModifier.PRIVATE
                initializer(
                    "%T.of(%T::class).nodeProp",
                    ClassName("$KIMMER_PACKAGE.graphql.meta", "ConnectionEdgeType"),
                    declaration.asClassName()
                )
            }.build()
        )
    }
}

private val finalDraftPrefixes = listOf("Sync", "Async")
