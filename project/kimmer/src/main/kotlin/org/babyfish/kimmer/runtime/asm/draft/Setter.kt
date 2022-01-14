package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeSetter(prop: ImmutableProp, args: GeneratorArgs) {
    val parameterType = prop.returnType.java
    val getter = prop.kotlinProp.getter.javaMethod!!
    val setterName = getter.name.let {
        if (it.startsWith("is")) {
            "set${it.substring(2)}"
        } else {
            "set${it.substring(3)}"
        }
    }
    val typeDesc = Type.getDescriptor(parameterType)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        setterName,
        "($typeDesc)V"
    ) {
        visitSetter(prop, args)
    }
}

internal fun MethodVisitor.visitSetter(prop: ImmutableProp, args: GeneratorArgs) {
    val local = when (prop.returnType.java) {
        Long::class.javaPrimitiveType -> 3
        Double::class.javaPrimitiveType -> 3
        else -> 2
    }
    visitMutableModelStorage(local, args)
    visitSetter(local, prop, args) {
        visitLoad(prop.returnType.java, 1)
    }
    visitInsn(Opcodes.RETURN)
}

internal fun MethodVisitor.visitSetter(
    modifiedLocal: Int,
    prop: ImmutableProp,
    args: GeneratorArgs,
    valueBlocK: MethodVisitor.() -> Unit
) {
    visitVarInsn(Opcodes.ALOAD, modifiedLocal)
    visitInsn(Opcodes.ICONST_1)
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        loadedName(prop),
        "Z"
    )
    visitVarInsn(Opcodes.ALOAD, modifiedLocal)
    valueBlocK()
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        prop.name,
        Type.getDescriptor(prop.returnType.java)
    )
}
