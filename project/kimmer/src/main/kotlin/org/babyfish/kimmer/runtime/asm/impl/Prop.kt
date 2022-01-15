package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.UnloadedException
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeProp(prop: ImmutableProp, ownerInternalName: String) {

    val desc = Type.getDescriptor(prop.returnType.java)
    val loadedName = loadedName(prop)
    val signature = prop.targetType?.takeIf { prop.isList }?.let {
        "Ljava/util/List<${Type.getDescriptor(it.kotlinType.java)}>;"
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
        visitCond(
            Opcodes.IFNE
        ) {
            visitThrow(UnloadedException::class, "The field '${prop.kotlinProp}' is unloaded")
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, prop.name, desc)
        visitReturn(prop.returnType.java)
    }
}