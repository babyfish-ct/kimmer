package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.*
import org.babyfish.kimmer.runtime.asm.visitReturn
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeGetter(prop: ImmutableProp, args: GeneratorArgs) {
    val getter = prop.kotlinProp.getter.javaMethod!!
    val returnType = prop.returnType.java
    writeMethod(
        Opcodes.ACC_PUBLIC,
        getter.name,
        "()${Type.getDescriptor(returnType)}"
    ) {
        visitGetter(prop, args)
    }
}

private fun MethodVisitor.visitGetter(prop: ImmutableProp, args: GeneratorArgs) {
    val getter = prop.kotlinProp.getter.javaMethod!!

    val loadValue: MethodVisitor.() -> Unit = {
        visitModelGetter(args)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            args.modelInternalName,
            getter.name,
            Type.getMethodDescriptor(getter),
            true
        )
    }

    if (prop.targetType !== null) {
        visitToDraft(prop, args, loadValue)
    } else {
        loadValue()
    }
    visitReturn(prop.returnType.java)
}

