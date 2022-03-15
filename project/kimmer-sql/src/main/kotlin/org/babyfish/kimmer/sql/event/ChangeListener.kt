package org.babyfish.kimmer.sql.event

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityType

fun interface ChangeListener {

    fun change(
        entityType: EntityType,
        oldEntity: Entity<*>,
        newEntity: Entity<*>
    )
}