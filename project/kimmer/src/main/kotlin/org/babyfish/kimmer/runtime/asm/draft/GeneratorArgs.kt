package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.asm.draftImplInternalName
import org.babyfish.kimmer.runtime.asm.implInternalName
import org.springframework.asm.Type

internal data class GeneratorArgs(
    val immutableType: ImmutableType
) {
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)
    val modelDescriptor = Type.getDescriptor(immutableType.kotlinType.java)
    val draftInternalName = Type.getInternalName(immutableType.draftInfo.abstractType)
    val modelImplInternalName = implInternalName(immutableType)
    val modelImplDescriptor = "L$modelImplInternalName;"
    val draftImplInternalName = draftImplInternalName(immutableType)
}