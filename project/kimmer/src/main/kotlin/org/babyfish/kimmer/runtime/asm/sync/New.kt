package org.babyfish.kimmer.runtime.asm.sync

import org.babyfish.kimmer.runtime.asm.KCLASS_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeNew() {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "new-UwztTBA",
        "($KCLASS_DESCRIPTOR)$KCLASS_DESCRIPTOR"
    ) {
        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ARETURN)
    }
}