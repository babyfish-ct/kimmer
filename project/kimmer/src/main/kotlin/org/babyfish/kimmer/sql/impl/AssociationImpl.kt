package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.UnloadedException
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.AssociationType

internal open class AssociationImpl<S, SID, T, TID>(
    type: AssociationType
): AssociationImplementor<S, SID, T, TID>(type), ImmutableSpi
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID>
{
    private var _source: S? = null

    private var _target: T? = null

    constructor(
        type: AssociationType,
        copyFrom: AssociationImplementor<S, SID, T, TID>
    ) : this(type) {
        _source = if (copyFrom.`{loaded}`(SOURCE)) copyFrom.source else null
        _target = if (copyFrom.`{loaded}`(TARGET)) copyFrom.target else null
    }

    override val id: AssociationId<SID, TID>
        get() = when {
            !`{loaded}`(SOURCE) ->
                throw UnloadedException("Cannot access association id because source is unloaded")
            !`{loaded}`(TARGET) ->
                throw UnloadedException("Cannot access association id because source is unloaded")
            else ->
                AssociationId(source.id, target.id)
        }

    override val source: S
        get() = _source ?: throw UnloadedException("'source' is not loaded")

    override val target: T
        get() = _target ?: throw UnloadedException("'target' is not loaded")

    override fun `{loaded}`(prop: String): Boolean =
        when (prop) {
            ID -> _source !== null && _target !== null
            SOURCE -> _source !== null
            TARGET -> _target !== null
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }

    fun setSource(value: S?) {
        _source = value
    }

    fun setTarget(value: T?) {
        _target = value
    }
}