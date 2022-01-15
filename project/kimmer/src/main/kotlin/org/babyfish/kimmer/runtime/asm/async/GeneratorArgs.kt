package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.asyncDraftImplInternalName
import org.babyfish.kimmer.runtime.asm.draftImplInternalName
import org.springframework.asm.Type

internal data class GeneratorArgs(
    val immutableType: ImmutableType
) {
    val internalName = asyncDraftImplInternalName(immutableType)
    val rawDraftImplInternalName = draftImplInternalName(immutableType)
    val rawDraftImplDescriptor = "L$rawDraftImplInternalName;"
    val modelDescriptor: String = Type.getDescriptor(immutableType.kotlinType.java)
}