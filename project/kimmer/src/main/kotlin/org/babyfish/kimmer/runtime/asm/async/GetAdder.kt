package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.AsyncDraft
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeGetAdder() {

    for (method in AsyncDraft::class.java.declaredMethods) {
        val name = method.name.takeIf { it.startsWith("getAdd-") } ?: continue
        writeMethod(
            Opcodes.ACC_PUBLIC,
            name,
            "(Ljava/util/List;)Ljava/util/List;"
        ) {
            visitVarInsn(Opcodes.ALOAD, 1)
            visitInsn(Opcodes.ARETURN)
        }
    }
}