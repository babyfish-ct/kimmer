package org.babyfish.kimmer.runtime.asm.impl

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeHashCode(type: ImmutableType) {

    val internalName = implInternalName(type)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "hashCode",
        "()I"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitInsn(Opcodes.ICONST_0)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            internalName,
            "hashCode",
            "(Z)I",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "hashCode",
        "(Z)I"
    ) {
        val hashSlot = 2
        val loadedSlot = 3
        val valueSlot = 4
        visitInsn(Opcodes.ICONST_0)
        visitVarInsn(Opcodes.ISTORE, hashSlot)

        for (prop in type.props.values) {

            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                internalName,
                loadedName(prop),
                "Z"
            )
            visitVarInsn(Opcodes.ISTORE, loadedSlot)

            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitCond(Opcodes.IFEQ) {
                visitVarInsn(Opcodes.ALOAD, 0)
                visitFieldInsn(
                    Opcodes.GETFIELD,
                    internalName,
                    prop.name,
                    Type.getDescriptor(prop.returnType.java)
                )
                visitStore(prop.returnType.java, valueSlot)

                val (primitiveType, boxInternalName) = primitiveTuples(prop.returnType.java)
                if (primitiveType === "") {
                    visitVarInsn(Opcodes.ALOAD, valueSlot)
                    visitCond(Opcodes.IFNULL) {
                        visitVarInsn(Opcodes.ILOAD, hashSlot)
                        visitLdcInsn(31)
                        visitInsn(Opcodes.IMUL)
                        val deepHashCodeBlock: () -> Unit = {
                            visitVarInsn(Opcodes.ALOAD, valueSlot)
                            visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/Object",
                                "hashCode",
                                "()I",
                                false
                            )
                        }
                        if (prop.targetType != null) {
                            visitVarInsn(Opcodes.ILOAD, 1)
                            visitCond(
                                Opcodes.IFNE,
                                { deepHashCodeBlock() },
                                {
                                    visitVarInsn(Opcodes.ALOAD, valueSlot)
                                    visitMethodInsn(
                                        Opcodes.INVOKESTATIC,
                                        "java/lang/System",
                                        "identityHashCode",
                                        "(Ljava/lang/Object;)I",
                                        false
                                    )
                                }
                            )
                        } else {
                            deepHashCodeBlock()
                        }
                        visitInsn(Opcodes.IADD)
                        visitVarInsn(Opcodes.ISTORE, hashSlot)
                    }
                } else {
                    visitVarInsn(Opcodes.ILOAD, hashSlot)
                    visitLdcInsn(31)
                    visitInsn(Opcodes.IMUL)
                    visitLoad(prop.returnType.java, valueSlot)
                    visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        boxInternalName,
                        "hashCode",
                        "($primitiveType)I",
                        false
                    )
                    visitInsn(Opcodes.IADD)
                    visitVarInsn(Opcodes.ISTORE, hashSlot)
                }
            }
        }
        visitVarInsn(Opcodes.ILOAD, hashSlot)
        visitInsn(Opcodes.IRETURN)
    }
}

internal fun ClassVisitor.writeEquals(type: ImmutableType) {

    val internalName = implInternalName(type)
    val modelInternalName = Type.getInternalName(type.kotlinType.java)
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    val immutableTypeDescriptor = Type.getDescriptor(ImmutableType::class.java)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "equals",
        "(Ljava/lang/Object;)Z"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ICONST_0)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            internalName,
            "equals",
            "(Ljava/lang/Object;Z)Z",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "equals",
        "(Ljava/lang/Object;Z)Z"
    ) {

        val spiSlot = 3
        val loadedSlot = 4

        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitCond(Opcodes.IF_ACMPNE) {
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.IRETURN)
        }

        visitVarInsn(Opcodes.ALOAD, 1)
        visitCond(Opcodes.IFNONNULL) {
            visitInsn(Opcodes.ICONST_0)
            visitInsn(Opcodes.IRETURN)
        }

        visitVarInsn(Opcodes.ALOAD, 1)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ASTORE, spiSlot)
        visitVarInsn(Opcodes.ALOAD, spiSlot)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{type}",
            "()$immutableTypeDescriptor",
            true
        )
        visitFieldInsn(
            Opcodes.GETSTATIC,
            internalName,
            "{immutableType}",
            immutableTypeDescriptor
        )
        visitCond(Opcodes.IF_ACMPEQ) {
            visitInsn(Opcodes.ICONST_0)
            visitInsn(Opcodes.IRETURN)
        }

        for (prop in type.props.values) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                internalName,
                loadedName(prop),
                "Z"
            )
            visitVarInsn(Opcodes.ISTORE, loadedSlot)

            visitVarInsn(Opcodes.ALOAD, spiSlot)
            visitLdcInsn(prop.name)
            visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                spiInternalName,
                "{loaded}",
                "(Ljava/lang/String;)Z",
                true
            )
            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitCond(Opcodes.IF_ICMPEQ) {
                visitInsn(Opcodes.ICONST_0)
                visitInsn(Opcodes.IRETURN)
            }

            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitCond(Opcodes.IFEQ) {
                val getter = prop.kotlinProp.getter.javaMethod!!
                val desc = Type.getDescriptor(getter.returnType)
                visitVarInsn(Opcodes.ALOAD, 0)
                visitFieldInsn(
                    Opcodes.GETFIELD,
                    internalName,
                    prop.name,
                    desc
                )
                visitVarInsn(Opcodes.ALOAD, 1)
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    modelInternalName,
                    getter.name,
                    "()$desc",
                    true
                )
                visitChanged(prop, Shallow.dynamic {
                    visitVarInsn(Opcodes.ILOAD, 2)
                }) {
                    visitInsn(Opcodes.ICONST_0)
                    visitInsn(Opcodes.IRETURN)
                }
            }
        }

        visitInsn(Opcodes.ICONST_1)
        visitInsn(Opcodes.IRETURN)
    }
}