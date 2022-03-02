package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeGetAdder() {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "getAdd-6l4VB_Q",
        "(Ljava/util/List;)Ljava/util/List;"
    ) {
        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ARETURN)
    }
}