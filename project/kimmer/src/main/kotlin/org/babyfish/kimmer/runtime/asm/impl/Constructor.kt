package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Label
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeConstructor() {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "()V",
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            OBJECT_INTERNAL_NAME,
            "<init>",
            "()V",
            false
        )
        visitInsn(Opcodes.RETURN)
    }
}

internal fun ClassVisitor.writeCopyConstructor(type: ImmutableType) {

    val implInternalName = implInternalName(type)

    val itfInternalName = Type.getInternalName(type.kotlinType.java)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "(L$itfInternalName;)V"
    ) {

        val implItfInternalName = Type.getInternalName(ImmutableSpi::class.java)

        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            OBJECT_INTERNAL_NAME,
            "<init>",
            "()V",
            false
        )

        val loadedSlot = 2

        for (prop in type.props.values) {

            var getter = prop.kotlinProp.getter.javaMethod!!

            visitVarInsn(Opcodes.ALOAD, 1)
            visitTypeInsn(Opcodes.CHECKCAST, implItfInternalName)
            visitLdcInsn(prop.name)
            visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                implItfInternalName,
                "{loaded}",
                "(Ljava/lang/String;)Z",
                true
            )
            visitVarInsn(Opcodes.ISTORE, loadedSlot)
            visitVarInsn(Opcodes.ALOAD, 0)
            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                implInternalName,
                loadedName(prop),
                "Z"
            )

            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitCond(Opcodes.IFEQ) {
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    itfInternalName,
                    getter.name,
                    Type.getMethodDescriptor(getter),
                    true
                )
                if (getter.returnType !== prop.returnType.java) {

                    visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(prop.returnType.java))
                }
                visitFieldInsn(
                    Opcodes.PUTFIELD,
                    implInternalName,
                    prop.name,
                    Type.getDescriptor(prop.returnType.java)
                )
            }
        }
        visitInsn(Opcodes.RETURN)
    }
}