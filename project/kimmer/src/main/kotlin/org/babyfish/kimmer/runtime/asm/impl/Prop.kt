package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.UnloadedException
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeProp(prop: ImmutableProp, ownerInternalName: String) {

    val desc = Type.getDescriptor(prop.javaReturnType)
    val loadedName = loadedName(prop)
    val signature = when { // Jackson need it
        prop.isList || prop.isScalarList -> "Ljava/util/List<${Type.getDescriptor(prop.javaReturnType)}>;"
        prop.isConnection -> "L$CONNECTION_INTERNAL_NAME<${Type.getDescriptor(prop.javaReturnType)}>;"
        else -> null
    }

    writeField(
        Opcodes.ACC_PROTECTED,
        prop.name,
        desc,
        signature
    )

    writeField(
        Opcodes.ACC_PROTECTED,
        loadedName,
        "Z"
    )

    val javaGetter = prop.kotlinProp.getter.javaMethod!!
    writeMethod(
        Opcodes.ACC_PUBLIC,
        javaGetter.name,
        Type.getMethodDescriptor(javaGetter),
        signature?.let { "()$signature" }
    ) {

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, loadedName, "Z")
        visitCondNotMatched(
            Opcodes.IFNE
        ) {
            visitThrow(UnloadedException::class, "The field '${prop.kotlinProp}' is unloaded")
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, prop.name, desc)
        visitReturn(prop.javaReturnType)
    }
}