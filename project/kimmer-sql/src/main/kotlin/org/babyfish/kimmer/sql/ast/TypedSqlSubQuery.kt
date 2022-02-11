package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.sql.Entity

interface TypedSqlSubQuery<P, PID, E, ID, R> :
    SqlSubQuery<P, PID, E, ID>,
    Expression<R>
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID>