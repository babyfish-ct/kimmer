package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeCreator(prop: ImmutableProp, args: GeneratorArgs) {

    val desc = Type.getDescriptor(prop.javaReturnType)

    val draftDesc = if (prop.isList || prop.isScalarList) {
        "Ljava/util/List;"
    } else {
        prop.targetType?.draftInfo?.abstractType?.let {
            Type.getDescriptor(it)
        } ?: Type.getDescriptor(prop.targetType!!.kotlinType.java)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        prop.name,
        "()$draftDesc"
    ) {

        val modifiedLocal = 1

        visitMutableModelStorage(modifiedLocal, args)
        val loadMutableValue: MethodVisitor.() -> Unit = {
            visitVarInsn(Opcodes.ALOAD, modifiedLocal)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.modelImplInternalName,
                prop.name,
                desc
            )
        }

        visitVarInsn(Opcodes.ALOAD, modifiedLocal)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.modelImplInternalName,
            loadedName(prop),
            "Z"
        )
        visitCondNotMatched(Opcodes.IFEQ) {

            loadMutableValue()
            visitCondNotMatched(Opcodes.IFNULL) {
                visitToDraft(prop, args, loadMutableValue)
                visitInsn(Opcodes.ARETURN)
            }
        }

        visitSetter(modifiedLocal, prop, args) {
            if (prop.isList || prop.isScalarList) {
                visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
                visitInsn(Opcodes.DUP)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/util/ArrayList",
                    "<init>",
                    "()V",
                    false
                )
            } else {
                val targetInternalName = implInternalName(prop.targetType!!)
                visitTypeInsn(Opcodes.NEW, targetInternalName)
                visitInsn(Opcodes.DUP)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    targetInternalName,
                    "<init>",
                    "()V",
                    false
                )
            }
        }
        visitToDraft(prop, args, loadMutableValue)
        visitInsn(Opcodes.ARETURN)
    }
}