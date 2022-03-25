package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.asm.draftContextName
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun MethodVisitor.visitToDraft(
    prop: ImmutableProp,
    args: GeneratorArgs,
    valueBlock: MethodVisitor.() -> Unit
) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        draftContextName(),
        Type.getDescriptor(DraftContext::class.java)
    )
    valueBlock()
    visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        Type.getInternalName(DraftContext::class.java),
        "toDraft",
        if (prop.isList || prop.isScalarList) {
            "(Ljava/util/List;)Ljava/util/List;"
        } else {
            "(${Type.getDescriptor(Immutable::class.java)})${Type.getDescriptor(Draft::class.java)}"
        },
        true
    )
    if (!prop.isList && !prop.isScalarList) {
        visitTypeInsn(
            Opcodes.CHECKCAST,
            Type.getInternalName(
            prop.targetType!!.draftInfo?.abstractType
                ?: prop.targetType!!.kotlinType.java
            )
        )
    }
}