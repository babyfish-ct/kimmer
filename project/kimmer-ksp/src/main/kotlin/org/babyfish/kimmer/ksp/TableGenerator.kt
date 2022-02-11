package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStreamWriter

class TableGenerator(
    private val codeGenerator: CodeGenerator,
    private val sysTypes: TableSysTypes,
    private val file: KSFile,
    private val modelClassDeclarations: List<KSClassDeclaration>
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
                            }
                            .build()
                    )
                    for (classDeclaration in modelClassDeclarations) {
                        val selfTypeName = classDeclaration.asClassName()
                        for (prop in classDeclaration.getAllProperties()) {
                            val propMeta = PropMeta.of(prop, sysTypes, true)
                            val receiverTypeName =
                                ClassName(
                                    KIMMER_SQL_AST_PACKAGE,
                                    if (propMeta.targetDeclaration === null) {
                                        "Table"
                                    } else {
                                        "JoinableTable"
                                    }
                                ).parameterizedBy(selfTypeName)
                            val returnTypeName =
                                propMeta.targetDeclaration?.let { tgt ->
                                    ClassName(KIMMER_SQL_AST_PACKAGE, "JoinableTable")
                                        .parameterizedBy(tgt.asClassName())
                                } ?: ClassName(KIMMER_SQL_AST_PACKAGE, "Expression")
                                    .parameterizedBy(propMeta.returnType)
                            addProperty(
                                PropertySpec
                                    .builder(prop.simpleName.asString(), returnTypeName)
                                    .apply {
                                        receiver(receiverTypeName)
                                        val code = when {
                                            propMeta.isReference -> "return joinReference(%T::%L)"
                                            propMeta.isList -> "return joinList(%T::%L)"
                                            propMeta.isConnection -> "return joinConnection(%T::%L)"
                                            else -> "return this[%T::%L]"
                                        }
                                        getter(
                                            FunSpec.getterBuilder().apply {
                                                modifiers += KModifier.INLINE
                                                addCode(code, selfTypeName, prop.simpleName.asString())
                                            }.build()
                                        )
                                    }
                                    .build()
                            )
                            if (propMeta.targetDeclaration !== null) {
                                addFunction(
                                    FunSpec
                                        .builder(prop.simpleName.asString())
                                        .apply {
                                            modifiers += KModifier.INLINE
                                            receiver(receiverTypeName)
                                            addParameter(
                                                ParameterSpec(
                                                    "joinType",
                                                    ClassName(KIMMER_SQL_AST_PACKAGE, "JoinType")
                                                )
                                            )
                                            returns(returnTypeName)
                                            val code = when {
                                                propMeta.isReference -> "return joinReference(%T::%L, joinType)"
                                                propMeta.isList -> "return joinList(%T::%L, joinType)"
                                                propMeta.isConnection -> "return joinConnection(%T::%L, joinType)"
                                                else -> error("Internal bug")
                                            }
                                            addCode(code, selfTypeName, prop.simpleName.asString())
                                        }
                                        .build()
                                )
                            }
                        }
                    }
                }.build()
            val writer = OutputStreamWriter(it, Charsets.UTF_8)
            fileSpec.writeTo(writer)
            writer.flush()
        }
    }
}