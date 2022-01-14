package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.jackson.ImmutableModule
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.IMMUTABLE_TYPE_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.OBJECT_MAPPER_DESCRIPTOR
import org.babyfish.kimmer.runtime.asm.OBJECT_MAPPER_INTERNAL_NAME
import org.babyfish.kimmer.runtime.asm.implInternalName
import org.babyfish.kimmer.runtime.asm.writeField
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeStaticBehaviors(type: ImmutableType) {

    val typeInternalName = Type.getInternalName(ImmutableType::class.java)

    writeField(
        Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
        "{immutableType}",
        IMMUTABLE_TYPE_DESCRIPTOR
    )

    writeField(
        Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
        "{objectMapper}",
        OBJECT_MAPPER_DESCRIPTOR
    )

    writeMethod(
        Opcodes.ACC_STATIC,
        "<clinit>",
        "()V"
    ) {
        visitLdcInsn(Type.getType(type.kotlinType.java))
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            typeInternalName,
            "of",
            "(Ljava/lang/Class;)$IMMUTABLE_TYPE_DESCRIPTOR",
            true
        )
        visitFieldInsn(
            Opcodes.PUTSTATIC,
            implInternalName(type),
            "{immutableType}",
            IMMUTABLE_TYPE_DESCRIPTOR
        )
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            ImmutableModule::class.java.`package`.name.replace('.', '/') + "/MapperKt",
            "immutableObjectMapper",
            "()$OBJECT_MAPPER_DESCRIPTOR",
            false
        )
        visitFieldInsn(
            Opcodes.PUTSTATIC,
            implInternalName(type),
            "{objectMapper}",
            OBJECT_MAPPER_DESCRIPTOR
        )
        visitInsn(Opcodes.RETURN)
    }

    writeRuntimeType(type)
    writeToString(type)
}

private fun ClassVisitor.writeRuntimeType(type: ImmutableType) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{type}",
        "()$IMMUTABLE_TYPE_DESCRIPTOR"
    ) {
        visitFieldInsn(
            Opcodes.GETSTATIC,
            implInternalName(type),
            "{immutableType}",
            IMMUTABLE_TYPE_DESCRIPTOR
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeToString(type: ImmutableType) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "toString",
        "()Ljava/lang/String;"
    ) {
        visitFieldInsn(
            Opcodes.GETSTATIC,
            implInternalName(type),
            "{objectMapper}",
            OBJECT_MAPPER_DESCRIPTOR
        )
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            OBJECT_MAPPER_INTERNAL_NAME,
            "writeValueAsString",
            "(Ljava/lang/Object;)Ljava/lang/String;",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}