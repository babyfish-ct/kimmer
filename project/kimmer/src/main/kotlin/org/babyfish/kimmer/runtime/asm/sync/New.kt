package org.babyfish.kimmer.runtime.asm.sync

import org.babyfish.kimmer.SyncDraft
import org.babyfish.kimmer.runtime.asm.KCLASS_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import kotlin.reflect.full.declaredFunctions

internal fun ClassVisitor.writeNew() {

    for (method in SyncDraft::class.java.declaredMethods) {
        val name = method.name.takeIf {
            it.startsWith("new") && it.indexOf("-") != -1
        } ?: continue
        writeMethod(
            Opcodes.ACC_PUBLIC,
            name,
            "($KCLASS_DESCRIPTOR)$KCLASS_DESCRIPTOR"
        ) {
            visitVarInsn(Opcodes.ALOAD, 1)
            visitInsn(Opcodes.ARETURN)
        }
    }
}