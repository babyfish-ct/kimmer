package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Draft

interface EntityDraft<out T: Entity<ID>, ID: Comparable<ID>>: Entity<ID>, Draft<T> {
    override var id: ID
}