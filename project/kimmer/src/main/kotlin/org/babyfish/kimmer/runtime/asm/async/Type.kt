package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.BYTECODE_VERSION
import org.babyfish.kimmer.runtime.asm.DRAFT_SPI_INTERNAL_NAME
import org.babyfish.kimmer.runtime.asm.rawDraftName
import org.babyfish.kimmer.runtime.asm.writeField
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeType(type: ImmutableType) {

    val args = GeneratorArgs(type)
    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        args.internalName,
        null,
        "java/lang/Object",
        arrayOf(
            Type.getInternalName(type.draftInfo.asyncType),
            DRAFT_SPI_INTERNAL_NAME
        )
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        rawDraftName(),
        args.rawDraftImplDescriptor
    )

    writeConstructor(args)
    writeProxyMethods(args)
    writeNew()

    visitEnd()
}