package org.babyfish.kimmer.runtime.asm

import com.fasterxml.jackson.databind.ObjectMapper
import org.babyfish.kimmer.CircularReferenceException
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.runtime.AsyncDraftContext
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.DraftSpi
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.runtime.SyncDraftContext
import org.springframework.asm.Type
import kotlin.reflect.KClass

internal val KCLASS_DESCRIPTOR = Type.getDescriptor(KClass::class.java)

internal val IMMUTABLE_DESCRIPTOR = Type.getDescriptor(Immutable::class.java)

internal val IMMUTABLE_SPI_INTERNAL_NAME = Type.getInternalName(ImmutableSpi::class.java)

internal val IMMUTABLE_TYPE_DESCRIPTOR = Type.getDescriptor(ImmutableType::class.java)

internal val DRAFT_DESCRIPTOR = Type.getDescriptor(Draft::class.java)

internal val DRAFT_SPI_INTERNAL_NAME = Type.getInternalName(DraftSpi::class.java)

internal val DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(DraftContext::class.java)

internal val DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(DraftContext::class.java)

internal val SYNC_DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(SyncDraftContext::class.java)

internal val SYNC_DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(SyncDraftContext::class.java)

internal val ASYNC_DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(AsyncDraftContext::class.java)

internal val ASYNC_DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(AsyncDraftContext::class.java)

internal val OBJECT_MAPPER_INTERNAL_NAME = Type.getInternalName(ObjectMapper::class.java)

internal val OBJECT_MAPPER_DESCRIPTOR = Type.getDescriptor(ObjectMapper::class.java)

internal val CRE_INTERNAL_NAME = Type.getInternalName(CircularReferenceException::class.java)

