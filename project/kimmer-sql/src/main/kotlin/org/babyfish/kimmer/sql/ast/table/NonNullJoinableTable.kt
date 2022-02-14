package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Selection

interface NonNullJoinableTable<E: Entity<ID>, ID: Comparable<ID>> :
    JoinableTable<E, ID>, NonNullTable<E, ID>, Selection<E>