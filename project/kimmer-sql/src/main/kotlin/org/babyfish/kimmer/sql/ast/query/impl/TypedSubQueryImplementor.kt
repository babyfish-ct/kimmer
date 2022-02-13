package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity

internal interface TypedSubQueryImplementor<P, PID, E, ID>
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    val baseQuery: SubQueryImpl<P, PID, E, ID>
}