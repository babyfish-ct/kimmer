package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity

interface SelectableTypedSubQuery<P, PID, E, ID, R: Any> : TypedSubQuery<P, PID, E, ID, R>
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    fun limit(limit: Int, offset: Int = 0): SelectableTypedSubQuery<P, PID, E, ID, R>
}