package org.babyfish.kimmer.sql.ast.table

import org.babyfish.kimmer.sql.Entity

interface NonNullSubQueryTable<E: Entity<ID>, ID: Comparable<ID>>
: SubQueryTable<E, ID>, NonNullJoinableTable<E, ID>