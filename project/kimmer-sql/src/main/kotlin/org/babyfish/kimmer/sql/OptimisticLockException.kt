package org.babyfish.kimmer.sql

class OptimisticLockException(
    val entity: Entity<*>
) : ExecutionException(
    "Cannot update the entity '$entity' because bad id or version"
)