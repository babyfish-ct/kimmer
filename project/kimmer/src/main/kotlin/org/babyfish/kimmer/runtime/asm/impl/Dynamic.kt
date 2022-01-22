package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeLoaded(type: ImmutableType) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{loaded}",
        "(Ljava/lang/String;)Z"
    ) {
        visitPropNameSwitch(type, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                implInternalName(type),
                loadedName(prop),
                "Z"
            )
            visitInsn(Opcodes.IRETURN)
        }
    }
}

internal fun ClassVisitor.writeValue(type: ImmutableType) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{get}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitPropNameSwitch(type, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            val getter = prop.kotlinProp.getter.javaMethod!!
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                implInternalName(type),
                getter.name,
                Type.getMethodDescriptor(getter),
                false
            )
            visitBox(getter.returnType)
            visitInsn(Opcodes.ARETURN)
        }
    }
}