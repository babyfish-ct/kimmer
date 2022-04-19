package org.babyfish.kimmer.sql

data class AssociationId<SID: Comparable<SID>, TID: Comparable<TID>>(
    val sourceId: SID,
    val targetId: TID
): Comparable<AssociationId<SID, TID>> {

    override fun compareTo(other: AssociationId<SID, TID>): Int {
        val cmp = sourceId.compareTo(other.sourceId)
        if (cmp != 0) {
            return cmp
        }
        return targetId.compareTo(other.targetId)
    }
}