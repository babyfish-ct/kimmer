package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.runtime.asm.baseName
import org.babyfish.kimmer.runtime.asm.modifiedName
import org.babyfish.kimmer.runtime.asm.visitCondNotMatched
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes

internal fun MethodVisitor.visitModelGetter(args: GeneratorArgs) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        modifiedName(),
        args.modelImplDescriptor
    )
    visitCondNotMatched(
        Opcodes.IFNULL,
        {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.draftImplInternalName,
                modifiedName(),
                args.modelImplDescriptor
            )
        },
        {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.draftImplInternalName,
                baseName(),
                args.modelDescriptor
            )
        }
    )
}

internal fun MethodVisitor.visitMutableModelStorage(local: Int, args: GeneratorArgs) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        modifiedName(),
        args.modelImplDescriptor
    )
    visitVarInsn(Opcodes.ASTORE, local)
    visitVarInsn(Opcodes.ALOAD, local)
    visitCondNotMatched(Opcodes.IFNONNULL) {
        visitTypeInsn(Opcodes.NEW, args.modelImplInternalName)
        visitInsn(Opcodes.DUP)
        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            args.modelImplInternalName,
            "<init>",
            "(${args.modelDescriptor})V",
            false
        )
        visitVarInsn(Opcodes.ASTORE, local)
        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, local)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            modifiedName(),
            args.modelImplDescriptor
        )
    }
}