package org.babyfish.kimmer.runtime

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.asm.*
import org.babyfish.kimmer.runtime.asm.BYTECODE_VERSION
import org.babyfish.kimmer.runtime.asm.async.asyncDraftImplementationOf
import org.babyfish.kimmer.runtime.asm.draft.draftImplementationOf
import org.babyfish.kimmer.runtime.asm.impl.implementationOf
import org.babyfish.kimmer.runtime.asm.implInternalName
import org.babyfish.kimmer.runtime.asm.sync.syncDraftImplementationOf
import org.babyfish.kimmer.runtime.asm.syncDraftImplInternalName
import org.babyfish.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.ClassWriter
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

internal interface Factory<T: Immutable> {
    fun create(): T
    fun createDraft(ctx: SyncDraftContext, o: T?): Draft<T>
    fun createDraft(ctx: AsyncDraftContext, o: T?): Draft<T>

    companion object {

        @JvmStatic
        fun <T: Immutable> of(type: KClass<T>): Factory<T> =
            factoryOf(type.java) as Factory<T>

        @JvmStatic
        fun <T: Immutable> of(type: Class<T>): Factory<T> =
            factoryOf(type) as Factory<T>
    }
}

private val cacheMap = mutableMapOf<Class<out Immutable>, Factory<out Immutable>>()

private val cacheLock = ReentrantReadWriteLock()

private fun factoryOf(type: Class<out Immutable>): Factory<out Immutable> =
    cacheLock.read {
        cacheMap[type]
    } ?: cacheLock.write {
        cacheMap[type]
            ?: createFactory(type).also {
                cacheMap[type] = it
            }
    }

private fun createFactory(type: Class<out Immutable>): Factory<out Immutable> {
    val immutableType = ImmutableType.of(type)
    implementationOf(immutableType.kotlinType.java)
    draftImplementationOf(immutableType.kotlinType.java)
    syncDraftImplementationOf(immutableType.kotlinType.java)
    asyncDraftImplementationOf(immutableType.kotlinType.java)
    val factoryType = createFactoryImplType(immutableType)
    return factoryType.getConstructor().newInstance() as Factory<out Immutable>
}

private fun createFactoryImplType(
    immutableType: ImmutableType
): Class<*> =
    ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            visit(
                BYTECODE_VERSION,
                Opcodes.ACC_PUBLIC,
                factoryInternalName(immutableType),
                null,
                "java/lang/Object",
                arrayOf(Type.getInternalName(Factory::class.java))
            )
            writeConstructor()
            writeCreate(immutableType)
            writeCreateDraft(immutableType, true)
            writeCreateDraft(immutableType, false)
            visitEnd()
        }
        .toByteArray()
        .defineClass()

private fun ClassVisitor.writeConstructor() {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "()V"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitInsn(Opcodes.RETURN)
    }
}

private fun ClassVisitor.writeCreate(immutableType: ImmutableType) {

    val implInternalName = implInternalName(immutableType)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "create",
        "()${Type.getDescriptor(Immutable::class.java)}"
    ) {
        visitTypeInsn(Opcodes.NEW, implInternalName)
        visitInsn(Opcodes.DUP)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            implInternalName,
            "<init>",
            "()V",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeCreateDraft(
    immutableType: ImmutableType,
    isAsync: Boolean
) {
    val internalName = if (isAsync) {
        asyncDraftImplInternalName(immutableType)
    } else {
        syncDraftImplInternalName(immutableType)
    }
    val targetDraftContextInternalName = if (isAsync) {
        ASYNC_DRAFT_CONTEXT_INTERNAL_NAME
    } else {
        SYNC_DRAFT_CONTEXT_INTERNAL_NAME
    }
    val targetDraftContextDescriptor = if (isAsync) {
        ASYNC_DRAFT_CONTEXT_DESCRIPTOR
    } else {
        SYNC_DRAFT_CONTEXT_DESCRIPTOR
    }
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "createDraft",
        "($targetDraftContextDescriptor$IMMUTABLE_DESCRIPTOR)$DRAFT_DESCRIPTOR"
    ) {

        visitTypeInsn(Opcodes.NEW, internalName)
        visitInsn(Opcodes.DUP)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitTypeInsn(Opcodes.CHECKCAST, targetDraftContextInternalName)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitTypeInsn(Opcodes.CHECKCAST, modelInternalName)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            internalName,
            "<init>",
            "(L$targetDraftContextInternalName;L$modelInternalName;)V",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun prefix(async: Boolean): String =
    if (async) {
        "Async"
    } else {
        "Sync"
    }
