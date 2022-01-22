package org.babyfish.kimmer.runtime

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType

internal interface ImmutableSpi {
    fun `{type}`(): ImmutableType
    fun `{loaded}`(prop: String): Boolean
    fun `{get}`(prop: String): Any?
    fun hashCode(shallow: Boolean): Int
    fun equals(other: Any?, shallow: Boolean): Boolean
}

internal interface DraftSpi: ImmutableSpi {
    fun `{draftContext}`(): DraftContext
    fun `{unload}`(prop: String): Unit
    fun `{getOrCreate}`(prop: String): Any
    fun `{set}`(prop: String, value: Any?): Unit
    fun `{resolve}`(): Immutable
}
