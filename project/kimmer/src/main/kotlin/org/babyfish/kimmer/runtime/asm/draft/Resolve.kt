package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.*
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeResolve(args: GeneratorArgs) {

    val desc = "()${Type.getDescriptor(Immutable::class.java)}"
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{resolve}",
        desc
    ) {

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            resolvingName(),
            "Z"
        )
        visitCond(Opcodes.IFEQ) {
            visitTypeInsn(Opcodes.NEW, CRE_INTERNAL_NAME)
            visitInsn(Opcodes.DUP)
            visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                CRE_INTERNAL_NAME,
                "<init>",
                "()V",
                false
            )
            visitInsn(Opcodes.ATHROW)
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitInsn(Opcodes.ICONST_1)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            resolvingName(),
            "Z"
        )

        visitTryFinally(1, {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.draftImplInternalName,
                "{resolveImpl}",
                desc,
                false
            )
            visitVarInsn(Opcodes.ASTORE, 1)
        }) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.ICONST_0)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                args.draftImplInternalName,
                resolvingName(),
                "Z"
            )
        }

        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ARETURN)

        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            args.draftImplInternalName,
            "{resolveImpl}",
            desc,
            false
        )
        visitInsn(Opcodes.ARETURN)
    }

    writeResolveImpl(args)
}

private fun ClassVisitor.writeResolveImpl(args: GeneratorArgs) {

    writeMethod(
        Opcodes.ACC_PRIVATE,
        "{resolveImpl}",
        "()${Type.getDescriptor(Immutable::class.java)}"
    ) {

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )
        visitVarInsn(Opcodes.ASTORE, baseSlot)

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            modifiedName(),
            args.modelImplDescriptor
        )
        visitVarInsn(Opcodes.ASTORE, modifiedSlot)

        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitCond(
            Opcodes.IFNONNULL,
            {
                for (prop in args.immutableType.props.values) {
                    if (prop.targetType !== null) {
                        visitResolveBaseProp(prop, args)
                    }
                }
            },
            {
                for (prop in args.immutableType.props.values) {
                    if (prop.targetType !== null) {
                        visitResolveModifiedProp(prop, args)
                    }
                }
            }
        )

        // store modified again because it may be changed
        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            modifiedName(),
            args.modelImplDescriptor
        )
        visitVarInsn(Opcodes.ASTORE, modifiedSlot)

        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitCond(Opcodes.IFNONNULL) {
            visitVarInsn(Opcodes.ALOAD, baseSlot)
            visitInsn(Opcodes.ARETURN)
        }

        visitVarInsn(Opcodes.ALOAD, baseSlot)
        visitTypeInsn(Opcodes.CHECKCAST, IMMUTABLE_SPI_INTERNAL_NAME)
        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitInsn(Opcodes.ICONST_1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            IMMUTABLE_SPI_INTERNAL_NAME,
            "equals",
            "(Ljava/lang/Object;Z)Z",
            true
        )
        visitCond(Opcodes.IFEQ) {
            visitVarInsn(Opcodes.ALOAD, baseSlot)
            visitInsn(Opcodes.ARETURN)
        }
        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitInsn(Opcodes.ARETURN)
    }
}

private fun MethodVisitor.visitResolveBaseProp(prop: ImmutableProp, args: GeneratorArgs) {

    visitVarInsn(Opcodes.ALOAD, baseSlot)
    visitTypeInsn(Opcodes.CHECKCAST, IMMUTABLE_SPI_INTERNAL_NAME)
    visitLdcInsn(prop.name)
    visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        IMMUTABLE_SPI_INTERNAL_NAME,
        "{loaded}",
        "(Ljava/lang/String;)Z",
        true
    )

    visitCond(Opcodes.IFEQ) {

        val getter = prop.kotlinProp.getter.javaMethod!!
        visitVarInsn(Opcodes.ALOAD, baseSlot)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            args.modelInternalName,
            getter.name,
            Type.getMethodDescriptor(getter),
            true
        )
        visitStore(prop.returnType.java, oldValueSlot)

        visitResolveValue(prop, args) {
            visitLoad(prop.returnType.java, oldValueSlot)
        }
        visitStore(prop.returnType.java, newValueSlot(prop))

        visitLoad(prop.returnType.java, oldValueSlot)
        visitLoad(prop.returnType.java, newValueSlot(prop))
        visitChanged(prop, Shallow.static(true)) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitLoad(prop.returnType.java, newValueSlot(prop))
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.draftImplInternalName,
                prop.kotlinProp.getter.javaMethod!!.name.let {
                    if (it.startsWith("is")) {
                        "set${it.substring(2)}"
                    } else {
                        "set${it.substring(3)}"
                    }
                },
                "(${Type.getDescriptor(prop.returnType.java)})V",
                false
            )
        }
    }
}

private fun MethodVisitor.visitResolveModifiedProp(prop: ImmutableProp, args: GeneratorArgs) {

    val propDesc = Type.getDescriptor(prop.returnType.java)

    visitVarInsn(Opcodes.ALOAD, modifiedSlot)
    visitResolveValue(prop, args) {
        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.modelImplInternalName,
            prop.name,
            propDesc
        )
    }
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        prop.name,
        propDesc
    )
}

private fun MethodVisitor.visitResolveValue(
    prop: ImmutableProp,
    args: GeneratorArgs,
    valueBlock: MethodVisitor.() -> Unit
) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        draftContextName(),
        DRAFT_CONTEXT_DESCRIPTOR
    )
    valueBlock()
    if (prop.isList) {
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            DRAFT_CONTEXT_INTERNAL_NAME,
            "resolve",
            "(Ljava/util/List;)Ljava/util/List;",
            true
        )
    } else {
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            DRAFT_CONTEXT_INTERNAL_NAME,
            "resolve",
            "($IMMUTABLE_DESCRIPTOR)$IMMUTABLE_DESCRIPTOR",
            true
        )
        visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(prop.returnType.java))
    }
}

private const val baseSlot = 1
private const val modifiedSlot = 2
private const val oldValueSlot = 3
private fun newValueSlot(prop: ImmutableProp) =
    when (prop.returnType.java) {
        Long::class.javaPrimitiveType -> 2
        Double::class.javaPrimitiveType -> 2
        else -> 1
    } + oldValueSlot

