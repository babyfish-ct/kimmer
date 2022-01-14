package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.asm.ASYNC_DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.asyncDraftImplInternalName
import org.babyfish.kimmer.runtime.asm.draftImplInternalName
import org.babyfish.kimmer.runtime.asm.rawDraftName
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeConstructor(args: GeneratorArgs) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "($ASYNC_DRAFT_CONTEXT_DESCRIPTOR${args.modelDescriptor})V"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitVarInsn(Opcodes.ALOAD, 0)
        visitTypeInsn(Opcodes.NEW, args.rawDraftImplInternalName)
        visitInsn(Opcodes.DUP)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            args.rawDraftImplInternalName,
            "<init>",
            "($DRAFT_CONTEXT_DESCRIPTOR${args.modelDescriptor})V",
            false
        )
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.internalName,
            rawDraftName(),
            args.rawDraftImplDescriptor
        )
        visitInsn(Opcodes.RETURN)
    }
}