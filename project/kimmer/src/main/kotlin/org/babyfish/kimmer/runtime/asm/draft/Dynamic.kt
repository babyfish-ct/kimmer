package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.asm.loadedName
import org.babyfish.kimmer.runtime.asm.visitPropNameSwitch
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.lang.IllegalArgumentException
import kotlin.reflect.jvm.javaGetter

internal fun ClassVisitor.writeDynamicGetter(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{get}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitPropNameSwitch(args.immutableType, { visitVarInsn(Opcodes.ALOAD, 1) }) { prop, _ ->
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.draftImplInternalName,
                prop.kotlinProp.javaGetter!!.name,
                Type.getMethodDescriptor(prop.kotlinProp.javaGetter),
                false
            )
            visitBox(prop.javaReturnType)
            visitInsn(Opcodes.ARETURN)
        }
    }
}

internal fun ClassVisitor.writeDynamicCreator(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{getOrCreate}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitPropNameSwitch(args.immutableType, { visitVarInsn(Opcodes.ALOAD, 1) }) { prop, _ ->
            if (prop.targetType === null) {
                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    args.draftImplInternalName,
                    prop.name,
                    Type.getMethodDescriptor(prop.kotlinProp.javaGetter),
                    false
                )
                visitBox(prop.javaReturnType)
            } else {
                val draftDesc = if (prop.isList) {
                    "Ljava/util/List;"
                } else {
                    prop.targetType?.draftInfo?.abstractType?.let {
                        Type.getDescriptor(it)
                    } ?: Type.getDescriptor(prop.targetType!!.kotlinType.java)
                }

                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    args.draftImplInternalName,
                    prop.name,
                    "()$draftDesc",
                    false
                )
            }
            visitInsn(Opcodes.ARETURN)
        }
    }
}

internal fun ClassVisitor.writeDynamicSetter(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{set}",
        "(Ljava/lang/String;Ljava/lang/Object;)V"
    ) {

        val mutableLocal = 3
        visitMutableModelStorage(mutableLocal, args)

        visitPropNameSwitch(args.immutableType, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            if (!prop.isNullable) {
                visitVarInsn(Opcodes.ALOAD, 2)
                visitCondNotMatched(Opcodes.IFNONNULL) {
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
            visitUnbox(prop.javaReturnType)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                args.modelImplInternalName,
                prop.name,
                Type.getDescriptor(prop.javaReturnType)
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