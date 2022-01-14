package org.babyfish.kimmer.runtime.asm.sync

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.SYNC_DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.draftImplInternalName
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeConstructor(type: ImmutableType) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "($SYNC_DRAFT_CONTEXT_DESCRIPTOR${Type.getDescriptor(type.kotlinType.java)})V"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            draftImplInternalName(type),
            "<init>",
            "($DRAFT_CONTEXT_DESCRIPTOR${Type.getDescriptor(type.kotlinType.java)})V",
            false
        )
        visitInsn(Opcodes.RETURN)
    }
}