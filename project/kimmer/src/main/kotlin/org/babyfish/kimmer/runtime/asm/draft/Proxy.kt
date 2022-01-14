package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.runtime.asm.IMMUTABLE_SPI_INTERNAL_NAME
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeRuntimeType(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    val desc = "()${Type.getDescriptor(ImmutableType::class.java)}"
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{type}",
        desc
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{type}",
            desc,
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

internal fun ClassVisitor.writeLoaded(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{loaded}",
        "(Ljava/lang/String;)Z"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{loaded}",
            "(Ljava/lang/String;)Z",
            true
        )
        visitInsn(Opcodes.IRETURN)
    }
}

internal fun ClassVisitor.writeValue(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{value}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{value}",
            "(Ljava/lang/String;)Ljava/lang/Object;",
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

internal fun ClassVisitor.writeHashCode(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "hashCode",
        "()I"
    ) {
        visitModelGetter(args)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "hashCode",
            "()I",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "hashCode",
        "(Z)I"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, IMMUTABLE_SPI_INTERNAL_NAME)
        visitVarInsn(Opcodes.ILOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            IMMUTABLE_SPI_INTERNAL_NAME,
            "hashCode",
            "(Z)I",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }
}

internal fun ClassVisitor.writeEquals(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "equals",
        "(Ljava/lang/Object;)Z"
    ) {
        visitModelGetter(args)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "equals",
            "(Ljava/lang/Object;)Z",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "equals",
        "(Ljava/lang/Object;Z)Z"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, IMMUTABLE_SPI_INTERNAL_NAME)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitVarInsn(Opcodes.ILOAD, 2)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            IMMUTABLE_SPI_INTERNAL_NAME,
            "equals",
            "(Ljava/lang/Object;Z)Z",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }
}

internal fun ClassVisitor.writeToString(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "toString",
        "()Ljava/lang/String;"
    ) {
        visitModelGetter(args)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "toString",
            "()Ljava/lang/String;",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}