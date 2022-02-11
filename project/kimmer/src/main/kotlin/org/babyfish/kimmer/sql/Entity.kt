package org.babyfish.kimmer.sql

import org.babyfish.kimmer.Immutable

interface Entity<ID: Comparable<ID>> : Immutable {
    val id: ID
}