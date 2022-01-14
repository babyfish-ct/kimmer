package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.runtime.asm.DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeContext(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{draftContext}",
        "()$DRAFT_CONTEXT_DESCRIPTOR"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            "{draftContext}",
            DRAFT_CONTEXT_DESCRIPTOR
        )
        visitInsn(Opcodes.ARETURN)
    }
}