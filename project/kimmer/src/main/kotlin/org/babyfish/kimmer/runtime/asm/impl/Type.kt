package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeType(type: ImmutableType) {

    val implInternalName = implInternalName(type)

    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        implInternalName(type),
        null,
        OBJECT_INTERNAL_NAME,
        arrayOf(Type.getInternalName(type.kotlinType.java), Type.getInternalName(ImmutableSpi::class.java))
    )

    writeConstructor()
    writeCopyConstructor(type)

    for (prop in type.props.values) {
        writeProp(prop, implInternalName)
    }

    writeLoaded(type)
    writeValue(type)

    writeHashCode(type)
    writeEquals(type)

    writeStaticBehaviors(type)

    visitEnd()
}