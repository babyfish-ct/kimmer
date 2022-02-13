package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStreamWriter

class TableGenerator(
    private val codeGenerator: CodeGenerator,
    private val sysTypes: TableSysTypes,
    private val file: KSFile,
    private val modelClassDeclarations: List<KSClassDeclaration>,
    private val collectionJoinOnlyForSubQuery: Boolean
) {

    fun generate(files: List<KSFile>) {
        val draftFileName =
            file.fileName.let {
                var lastDotIndex = it.lastIndexOf('.')
                if (lastDotIndex === -1) {
                    "$it$TABLE_SUFFIX"
                } else {
                    "${it.substring(0, lastDotIndex)}$TABLE_SUFFIX"
                }
            }
        codeGenerator.createNewFile(
            Dependencies(false, *files.toTypedArray()),
            file.packageName.asString(),
            draftFileName
        ).use { stream ->
            val entityIDTypeNameProvider = EntityIDTypeNameProvider()
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
                        val idProp = classDeclaration.getAllProperties().first {
                            it.simpleName.asString() == "id"
                        }
                        val nonIdMap = classDeclaration.getAllProperties()
                            .filter { it.simpleName.asString() != "id" }
                            .associateBy({it}) {
                            PropMeta.of(it, sysTypes, true)
                        }
                        addGetProps(
                            classDeclaration,
                            idProp,
                            PropMeta.of(idProp, sysTypes, true),
                            entityIDTypeNameProvider
                        )
                        for ((prop, propMeta) in nonIdMap) {
                            if (propMeta.targetDeclaration === null) {
                                addGetProps(classDeclaration, prop, propMeta, entityIDTypeNameProvider)
                            }
                        }
                        for ((prop, propMeta) in nonIdMap) {
                            if (propMeta.targetDeclaration !== null) {
                                addJoinProps(classDeclaration, prop, propMeta, entityIDTypeNameProvider)
                            }
                        }
                    }
                }.build()
            val writer = OutputStreamWriter(stream, Charsets.UTF_8)
            fileSpec.writeTo(writer)
            writer.flush()
        }
    }

    private fun FileSpec.Builder.addGetProps(
        classDeclaration: KSClassDeclaration,
        prop: KSPropertyDeclaration,
        propMeta: PropMeta,
        entityIDTypeNameProvider: EntityIDTypeNameProvider
    ) {
        val selfTypeName = classDeclaration.asClassName()
        val receiverTypeName =
            sysTypes.tableType.asClassName().parameterizedBy(
                selfTypeName,
                entityIDTypeNameProvider[classDeclaration]
            )
        val returnTypeName = ClassName(KIMMER_SQL_AST_PACKAGE, "Expression")
            .parameterizedBy(propMeta.returnType)
        val nonNullReceiverTypeName =
            if (!propMeta.isNullable) {
                sysTypes.nonNullTableType.asClassName().parameterizedBy(
                    selfTypeName,
                    entityIDTypeNameProvider[classDeclaration]
                )
            } else {
                null
            }
        if (nonNullReceiverTypeName !== null) {
            addProperty(
                PropertySpec
                    .builder(
                        prop.simpleName.asString(),
                        ClassName(KIMMER_SQL_AST_PACKAGE, "NonNullExpression")
                            .parameterizedBy(propMeta.returnType)
                    )
                    .apply {
                        receiver(nonNullReceiverTypeName)
                        getter(
                            FunSpec.getterBuilder().apply {
                                modifiers += KModifier.INLINE
                                addCode("return this.get(%T::%L)", selfTypeName, prop.simpleName.asString())
                            }.build()
                        )
                    }
                    .build()
            )
        }
        addProperty(
            PropertySpec
                .builder(prop.simpleName.asString(), returnTypeName)
                .apply {
                    receiver(receiverTypeName)
                    getter(
                        FunSpec.getterBuilder().apply {
                            modifiers += KModifier.INLINE
                            addCode("return this.`get?`(%T::%L)", selfTypeName, prop.simpleName.asString())
                        }.build()
                    )
                }
                .build()
        )
    }

    private fun FileSpec.Builder.addJoinProps(
        classDeclaration: KSClassDeclaration,
        prop: KSPropertyDeclaration,
        propMeta: PropMeta,
        entityIDTypeNameProvider: EntityIDTypeNameProvider
    ) {
        val selfTypeName = classDeclaration.asClassName()
        val nonNullReceiverTypeName =
            if (collectionJoinOnlyForSubQuery && (propMeta.isList || propMeta.isConnection)) {
                sysTypes.nonNullSubQueryTableType
            } else {
                sysTypes.nonNullJoinableTableType
            }.asClassName().parameterizedBy(
                selfTypeName,
                entityIDTypeNameProvider[classDeclaration]
            )
        val receiverTypeName =
            if (collectionJoinOnlyForSubQuery && (propMeta.isList || propMeta.isConnection)) {
                sysTypes.subQueryTableType
            } else {
                sysTypes.joinableTableType
            }.asClassName().parameterizedBy(
                selfTypeName,
                entityIDTypeNameProvider[classDeclaration]
            )
        val tgt = propMeta.targetDeclaration!!
        val nonNullReturnTypeName = sysTypes.nonNullJoinableTableType.asClassName()
            .parameterizedBy(
                tgt.asClassName(),
                entityIDTypeNameProvider[tgt]
            )
        val returnTypeName = sysTypes.joinableTableType.asClassName()
            .parameterizedBy(
                tgt.asClassName(),
                entityIDTypeNameProvider[tgt]
            )
        val joinFunName = when {
            propMeta.isReference -> "joinReference"
            propMeta.isList -> "joinList"
            propMeta.isConnection -> "joinConnection"
            else -> error("Internal bug")
        }
        if (!propMeta.isNullable) {
            addProperty(
                PropertySpec
                    .builder(prop.simpleName.asString(), nonNullReturnTypeName)
                    .apply {
                        receiver(nonNullReceiverTypeName)
                        getter(
                            FunSpec.getterBuilder().apply {
                                modifiers += KModifier.INLINE
                                addCode("return $joinFunName(%T::%L)", selfTypeName, prop.simpleName.asString())
                            }.build()
                        )
                    }
                    .build()
            )
        }
        addProperty(
            PropertySpec
                .builder(prop.simpleName.asString(), returnTypeName)
                .apply {
                    receiver(receiverTypeName)
                    getter(
                        FunSpec.getterBuilder().apply {
                            modifiers += KModifier.INLINE
                            addCode("return $joinFunName(%T::%L)", selfTypeName, prop.simpleName.asString())
                        }.build()
                    )
                }
                .build()
        )
        addProperty(
            PropertySpec
                .builder("${prop.simpleName.asString()}?", returnTypeName)
                .apply {
                    receiver(receiverTypeName)
                    getter(
                        FunSpec.getterBuilder().apply {
                            modifiers += KModifier.INLINE
                            addCode("return `$joinFunName?`(%T::%L)", selfTypeName, prop.simpleName.asString())
                        }.build()
                    )
                }
                .build()
        )
    }
}