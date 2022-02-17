package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Selection

internal interface TypedSubQueryImplementor<P, PID, E, ID, R> : TypedQueryImplementor, Selection<R>
where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    val baseQuery: SubMutableQueryImpl<P, PID, E, ID>
}