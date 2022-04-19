package org.babyfish.kimmer.sql.impl

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.babyfish.kimmer.UnloadedException
import org.babyfish.kimmer.jackson.AssociationSerializer
import org.babyfish.kimmer.runtime.ImmutableSpi
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.AssociationId
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.AssociationType

@JsonSerialize(using = AssociationSerializer::class)
internal abstract class AssociationImplementor<S, SID, T, TID>(
    val type: AssociationType
) : Association<S, SID, T, TID>, ImmutableSpi
    where S: Entity<SID>, T: Entity<TID>, SID: Comparable<SID>, TID: Comparable<TID> {

    override fun `{type}`(): AssociationType =
        type

    override fun `{get}`(prop: String): Any? =
        when (prop) {
            ID -> id
            SOURCE -> source
            TARGET -> target
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }

    override fun hashCode(): Int {
        var hash = 1
        hash = 31 * hash + if (`{loaded}`(SOURCE)) {
            source.hashCode()
        } else {
            0
        }
        hash = 31 * hash + if (`{loaded}`(TARGET)) {
            target.hashCode()
        } else {
            0
        }
        return hash
    }

    override fun hashCode(shallow: Boolean): Int =
        if (shallow) {
            var hash = 1
            hash = 31 * hash + if (`{loaded}`(SOURCE)) {
                System.identityHashCode(source)
            } else {
                0
            }
            hash = 31 * hash + if (`{loaded}`(TARGET)) {
                System.identityHashCode(target)
            } else {
                0
            }
            hash
        } else {
            hashCode()
        }

    override fun equals(other: Any?): Boolean =
        when {
            this === other ->
                true
            other !is AssociationImplementor<*, *, *, *> ->
                false
            else ->
                `{loaded}`(SOURCE) && other.`{loaded}`(SOURCE) &&
                    (!`{loaded}`(SOURCE) || source == other.source) &&
                    `{loaded}`(TARGET) && other.`{loaded}`(TARGET) &&
                    (!`{loaded}`(TARGET) || target == other.target)
        }

    override fun equals(other: Any?, shallow: Boolean): Boolean =
        if (shallow) {
            when {
                this === other ->
                    true
                other !is AssociationImplementor<*, *, *, *> ->
                    false
                else ->
                    `{loaded}`(SOURCE) && other.`{loaded}`(SOURCE) &&
                        (!`{loaded}`(SOURCE) || source === other.source) &&
                        `{loaded}`(TARGET) && other.`{loaded}`(TARGET) &&
                        (!`{loaded}`(TARGET) || target === other.target)
            }
        } else {
            equals(other)
        }

    override fun toString(): String =
        IMMUTABLE_MAPPER.writeValueAsString(this)
}
