package org.babyfish.kimmer.runtime.asm.async

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.DraftSpi
import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.asm.DRAFT_SPI_INTERNAL_NAME
import org.babyfish.kimmer.runtime.asm.visitLoad
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeProxyMethods(args: GeneratorArgs) {

    for (prop in args.immutableType.props.values) {
        writeProxyProp(prop, args)
    }

    for (method in DraftSpi::class.java.methods) {
        if (!Modifier.isStatic(method.modifiers)) {
            writeProxyMethod(method, args, RawType(DRAFT_SPI_INTERNAL_NAME, true))
        }
    }

    writeProxyMethod(Any::hashCode.javaMethod!!, args)
    writeProxyMethod(Any::equals.javaMethod!!, args)
    writeProxyMethod(Any::toString.javaMethod!!, args)
}

private fun ClassVisitor.writeProxyProp(
    prop: ImmutableProp,
    args: GeneratorArgs
) {
    val getter = prop.kotlinProp.getter.javaMethod!!
    val getterDesc = Type.getMethodDescriptor(getter)
    val setterDesc = "(${Type.getDescriptor(prop.javaReturnType)})V"
    val signature = prop.targetType?.takeIf { prop.isList || prop.isScalarList }?.let {
        "Ljava/util/List<${Type.getDescriptor(it.kotlinType.java)}>;"
    }
    val getterSignature = signature?.let { "()$signature" }
    val setterSignature = signature?.let { "($signature)V" }
    val setterName = getter.name.let {
        val offset = if (it.startsWith("is")) {
            2
        } else {
            3
        }
        "set${getter.name.substring(offset)}"
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        getter.name,
        getterDesc,
        getterSignature
    ) {
        val lockSlot = 1
        val resultSlot = 2
        visitLock(lockSlot, args) {
            visitGetRawDraft(args)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.rawDraftImplInternalName,
                getter.name,
                getterDesc,
                false
            )
            visitStore(prop.javaReturnType, resultSlot)
        }
        visitLoad(prop.javaReturnType, resultSlot)
        visitReturn(prop.javaReturnType)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        setterName,
        setterDesc,
        setterSignature
    ) {
        val lockSlot = 1 + Type.getArgumentsAndReturnSizes(setterDesc) shr 2
        visitLock(lockSlot, args) {
            visitGetRawDraft(args)
            visitLoad(prop.javaReturnType, 1)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.rawDraftImplInternalName,
                setterName,
                setterDesc,
                false
            )
        }
        visitInsn(Opcodes.RETURN)
    }

    if (prop.isAssociation || prop.isScalarList) {
        val targetDesc = prop
            .targetType
            ?.takeIf {
                prop.isList || prop.isReference
            }
            ?.draftInfo
            ?.abstractType
            ?.let { abstractDraftType ->
                Type.getDescriptor(abstractDraftType)
            }
            ?: Type.getDescriptor(prop.elementType.java)

        val funDesc = if (prop.isConnection) {
            "()$CONNECTION_DESCRIPTOR"
        } else if (prop.isList || prop.isScalarList) {
            "()Ljava/util/List;"
        } else {
            "()$targetDesc"
        }
        val funSignature = if (prop.isConnection) {
            "()L$CONNECTION_INTERNAL_NAME<$targetDesc>;"
        } else if (prop.isList || prop.isScalarList) {
            "()Ljava/util/List<$targetDesc>;"
        } else {
            null
        }
        writeMethod(
            Opcodes.ACC_PUBLIC,
            prop.name,
            funDesc,
            funSignature
        ) {
            val lockSlot = 1
            val resultSlot = 2
            visitLock(lockSlot, args) {
                visitGetRawDraft(args)
                visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    args.rawDraftImplInternalName,
                    prop.name,
                    funDesc,
                    false
                )
                visitStore(prop.javaReturnType, resultSlot)
            }
            visitLoad(prop.javaReturnType, resultSlot)
            visitReturn(prop.javaReturnType)
        }
    }
}

private fun ClassVisitor.writeProxyMethod(
    method: Method,
    args: GeneratorArgs,
    rawType: RawType? = null
) {
    val desc = Type.getMethodDescriptor(method)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        method.name,
        desc
    ) {
        val lockSlot = Type.getArgumentsAndReturnSizes(desc) shr 2
        val resultSlot = lockSlot + 1
        visitLock(lockSlot, args) {
            visitGetRawDraft(args)
            if (rawType !== null) {
                visitTypeInsn(Opcodes.CHECKCAST, rawType.internalName)
            }
            var local = 1
            for (parameterType in method.parameterTypes) {
                visitLoad(parameterType, local)
                local += parameterType.bytecodeSize
            }
            visitMethodInsn(
                if (rawType?.itf == true) {
                    Opcodes.INVOKEINTERFACE
                } else {
                    Opcodes.INVOKEVIRTUAL
                },
                rawType?.internalName ?: args.rawDraftImplInternalName,
                method.name,
                desc,
                rawType?.itf === true
            )
            visitStore(method.returnType, resultSlot)
        }
        visitLoad(method.returnType, resultSlot)
        visitReturn(method.returnType)
    }
}

private fun MethodVisitor.visitLock(
    lockSlot: Int,
    args: GeneratorArgs,
    block: MethodVisitor.() -> Unit
) {
    visitGetRawDraft(args)
    visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        args.rawDraftImplInternalName,
        "{draftContext}",
        "()$DRAFT_CONTEXT_DESCRIPTOR",
        false
    )
    visitVarInsn(Opcodes.ASTORE, lockSlot)

    visitVarInsn(Opcodes.ALOAD, lockSlot)
    visitInsn(Opcodes.MONITORENTER)
    visitTryFinally(
        lockSlot + 1,
        block
    ) {
        visitVarInsn(Opcodes.ALOAD, lockSlot)
        visitInsn(Opcodes.MONITOREXIT)
    }
}

private val Class<*>.bytecodeSize: Int
    get() = when (this) {
        Long::class.javaPrimitiveType -> 2
        Double::class.javaPrimitiveType -> 2
        else -> 1
    }

private data class RawType(
    val internalName: String,
    val itf: Boolean
)