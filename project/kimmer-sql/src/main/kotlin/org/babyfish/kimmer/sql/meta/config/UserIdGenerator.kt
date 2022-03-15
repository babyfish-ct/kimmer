package org.babyfish.kimmer.sql.meta.config

class UserIdGenerator<ID: Comparable<ID>>(
    val get: () -> ID
): IdGenerator