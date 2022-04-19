package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.CircularReferenceException
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.produceDraft
import org.babyfish.kimmer.runtime.DraftContext
import org.babyfish.kimmer.runtime.DraftSpi
import org.babyfish.kimmer.sql.AssociationDraft
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.AssociationType
import java.lang.UnsupportedOperationException

internal open class AssociationDraftImpl<S, SID, T, TID>(
    private val draftContext: DraftContext,
    type: AssociationType,
    base: AssociationImplementor<S, SID, T, TID>?
) : AssociationImplementor<S, SID, T, TID>(type), AssociationDraft<S, SID, T, TID>, DraftSpi
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {

    private val base: AssociationImplementor<S, SID, T, TID> = base ?: AssociationImpl(type)

    private var modified: AssociationImpl<S, SID, T, TID>? = null

    private var resolving = false

    private inline val immutable: AssociationImplementor<S, SID, T, TID>
        get() = modified ?: base

    private inline val mutable: AssociationImpl<S, SID, T, TID>
        get() = modified ?: AssociationImpl(`{type}`(), base).also {
            modified = it
        }

    override var id: AssociationId<SID, TID>
        get() = immutable.id
        set(value) {
            throw UnsupportedOperationException("Cannot set id of association object")
        }

    @Suppress("UNCHECKED_CAST")
    override var source: S
        get() = draftContext.toDraft(immutable.source) as S
        set(value) { mutable.setSource(value) }

    @Suppress("UNCHECKED_CAST")
    override var target: T
        get() = draftContext.toDraft(immutable.target) as T
        set(value) { mutable.setTarget(value) }

    override fun `{draftContext}`(): DraftContext =
        draftContext

    override fun `{loaded}`(prop: String): Boolean =
        immutable.`{loaded}`(prop)

    @Suppress("UNCHECKED_CAST")
    override fun `{get}`(prop: String): Any? =
        super.`{get}`(prop)?.let {
            when (it) {
                is Immutable -> draftContext.toDraft(it)
                else -> it
            }
        }

    override fun `{getOrCreate}`(prop: String): Any =
        when (prop) {
            ID ->
                if (immutable.`{loaded}`(ID)) {
                    immutable.id
                } else {
                    throw UnsupportedOperationException("Association.id does not support getOrCreate")
                }
            SOURCE ->
                produceDraft(type.sourceType.kotlinType) {}
            TARGET ->
                produceDraft(type.targetType.kotlinType) {}
            else ->
                throw IllegalArgumentException("No such prop '$prop'")
        }

    override fun `{unload}`(prop: String) {
        when (prop) {
            ID ->
                throw UnsupportedOperationException("Cannot unload id of association object")
            SOURCE ->
                if (immutable.`{loaded}`(SOURCE)) {
                    mutable.setSource(null)
                }
            TARGET ->
                if (immutable.`{loaded}`(TARGET)) {
                    mutable.setTarget(null)
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun `{set}`(prop: String, value: Any?) {
        when (prop) {
            ID -> throw UnsupportedOperationException("Cannot set id of association object")
            SOURCE -> mutable.setSource(value as S)
            TARGET -> mutable.setTarget(value as T)
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }
    }

    override fun `{resolve}`(): Immutable {
        if (resolving) {
            throw CircularReferenceException()
        }
        resolving = true
        try {
            if (immutable.`{loaded}`(SOURCE)) {
                val unresolved = immutable.source
                val resolved = draftContext.resolve(unresolved)
                if (unresolved !== resolved) {
                    mutable.setSource(resolved)
                }
            }
            if (immutable.`{loaded}`(TARGET)) {
                val unresolved = immutable.target
                val resolved = draftContext.resolve(unresolved)
                if (unresolved !== resolved) {
                    mutable.setTarget(resolved)
                }
            }
            val modified = this.modified
            return if (modified === null || base.equals(modified, true)) {
                base
            } else {
                modified
            }
        } finally {
            resolving = false
        }
    }
}