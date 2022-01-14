package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.runtime.asm.rawDraftName
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes

internal fun MethodVisitor.visitGetRawDraft(args: GeneratorArgs) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.internalName,
        rawDraftName(),
        args.rawDraftImplDescriptor
    )
}