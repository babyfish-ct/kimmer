package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.sql.Entity
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeSetter(prop: ImmutableProp, args: GeneratorArgs) {
    val parameterType = prop.javaReturnType
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
    val isId = prop.name == "id" && Entity::class.java.isAssignableFrom(prop.declaringType.kotlinType.java)
    if (isId) {
        writeMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_BRIDGE or Opcodes.ACC_SYNTHETIC,
            setterName,
            "($COMPARABLE_DESCRIPTOR)V"
        ) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitVarInsn(Opcodes.ALOAD, 1)
            visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parameterType))
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.draftImplInternalName,
                setterName,
                "($typeDesc)V",
                false
            )
            visitInsn(Opcodes.RETURN)
        }
    }
}

internal fun MethodVisitor.visitSetter(prop: ImmutableProp, args: GeneratorArgs) {
    val local = when (prop.javaReturnType) {
        Long::class.javaPrimitiveType -> 3
        Double::class.javaPrimitiveType -> 3
        else -> 2
    }
    visitMutableModelStorage(local, args)
    visitSetter(local, prop, args) {
        visitLoad(prop.javaReturnType, 1)
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
        Type.getDescriptor(prop.javaReturnType)
    )
}
