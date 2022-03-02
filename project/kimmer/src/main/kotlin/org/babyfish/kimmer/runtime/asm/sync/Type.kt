package org.babyfish.kimmer.runtime.asm.sync

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.asm.BYTECODE_VERSION
import org.babyfish.kimmer.runtime.asm.DRAFT_SPI_INTERNAL_NAME
import org.babyfish.kimmer.runtime.asm.draftImplInternalName
import org.babyfish.kimmer.runtime.asm.syncDraftImplInternalName
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeType(type: ImmutableType) {

    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        syncDraftImplInternalName(type),
        null,
        draftImplInternalName(type),
        type.draftInfo?.syncType?.let {
            arrayOf(
                Type.getInternalName(it),
                DRAFT_SPI_INTERNAL_NAME
            )
        } ?: arrayOf(
            SYNC_DRAFT_INTERNAL_NAME,
            Type.getInternalName(type.kotlinType.java),
            DRAFT_SPI_INTERNAL_NAME
        )
    )

    writeConstructor(type)
    writeNew()
    writeGetAdder()

    visitEnd()
}

