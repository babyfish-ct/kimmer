package org.babyfish.kimmer.runtime.asm.draft

import org.babyfish.kimmer.runtime.*
import org.babyfish.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeType(args: GeneratorArgs) {
    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PROTECTED,
        args.draftImplInternalName,
        null,
        OBJECT_INTERNAL_NAME,
        arrayOf(args.draftInternalName, Type.getInternalName(DraftSpi::class.java))
    )

    writeField(
        Opcodes.ACC_PROTECTED,
        draftContextName(),
        Type.getDescriptor(DraftContext::class.java)
    )

    writeField(
        Opcodes.ACC_PROTECTED,
        baseName(),
        args.modelDescriptor
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        modifiedName(),
        args.modelImplDescriptor
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        resolvingName(),
        "Z"
    )

    writeConstructor(args)
    for (prop in args.immutableType.props.values) {
        writeGetter(prop, args)
        writeSetter(prop, args)
        if (prop.isAssociation || prop.isScalarList) {
            writeCreator(prop, args)
        }
    }

    writeRuntimeType(args)
    writeLoaded(args)

    writeHashCode(args)
    writeEquals(args)
    writeToString(args)

    writeContext(args)
    writeResolve(args)

    writeDynamicGetter(args)
    writeDynamicCreator(args)
    writeDynamicSetter(args)

    writeUnload(args)

    visitEnd()
}