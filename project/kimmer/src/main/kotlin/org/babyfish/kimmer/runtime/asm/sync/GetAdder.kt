package org.babyfish.kimmer.runtime.asm.sync

import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeGetAdder() {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "getAdd-AY-hkMI",
        "(Ljava/util/List;)Ljava/util/List;"
    ) {
        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ARETURN)
    }
}