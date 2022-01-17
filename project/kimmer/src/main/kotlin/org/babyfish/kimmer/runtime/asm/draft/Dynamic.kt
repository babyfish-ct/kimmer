package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.asm.loadedName
import org.babyfish.kimmer.runtime.asm.visitPropNameSwitch
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.lang.IllegalArgumentException

internal fun ClassVisitor.writeSetValue(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{value}",
        "(Ljava/lang/String;Ljava/lang/Object;)V"
    ) {

        val mutableLocal = 3
        visitMutableModelStorage(mutableLocal, args)

        visitPropNameSwitch(args.immutableType, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            if (!prop.isNullable) {
                visitVarInsn(Opcodes.ALOAD, 2)
                visitCond(Opcodes.IFNONNULL) {
                    visitThrow(
                        IllegalArgumentException::class,
                        "Cannot set null to the prop '${prop.kotlinProp}' whose type is non-nullable"
                    )
                }
            }
            visitVarInsn(Opcodes.ALOAD, mutableLocal)
            visitInsn(Opcodes.ICONST_1)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                args.modelImplInternalName,
                loadedName(prop),
                "Z"
            )
            visitVarInsn(Opcodes.ALOAD, mutableLocal)
            visitVarInsn(Opcodes.ALOAD, 2)
            visitUnbox(prop.returnType.java)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                args.modelImplInternalName,
                prop.name,
                Type.getDescriptor(prop.returnType.java)
            )
            visitInsn(Opcodes.RETURN)
        }
    }
}

internal fun ClassVisitor.writeUnload(args: GeneratorArgs) {

    val mutableLocal = 2

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{unload}",
        "(Ljava/lang/String;)V"
    ) {
        visitMutableModelStorage(mutableLocal, args)

        visitPropNameSwitch(args.immutableType, {
            visitVarInsn(Opcodes.ALOAD, 1)
        }) { prop, _ ->
            visitVarInsn(Opcodes.ALOAD, mutableLocal)
            visitInsn(Opcodes.ICONST_0)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                args.modelImplInternalName,
                loadedName(prop),
                "Z"
            )
            visitInsn(Opcodes.RETURN)
        }
        visitInsn(Opcodes.RETURN)
    }
}