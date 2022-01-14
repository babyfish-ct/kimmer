package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.runtime.asm.DRAFT_CONTEXT_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.baseName
import org.babyfish.kimmer.runtime.asm.draftContextName
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeConstructor(args: GeneratorArgs) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "($DRAFT_CONTEXT_DESCRIPTOR${args.modelDescriptor})V"
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
        visitVarInsn(Opcodes.ALOAD, 1)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            draftContextName(),
            DRAFT_CONTEXT_DESCRIPTOR
        )

        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )

        visitInsn(Opcodes.RETURN)
    }
}