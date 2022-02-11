package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity

interface TypedSqlSubQuery<
    P: Entity<PID>,
    PID: Comparable<PID>,
    E: Entity<ID>,
    ID: Comparable<ID>,
    R
> : SqlSubQuery<P, PID, E, ID>, Expression<R>