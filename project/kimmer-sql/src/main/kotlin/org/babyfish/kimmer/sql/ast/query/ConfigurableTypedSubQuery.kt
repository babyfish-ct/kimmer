package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity

interface ConfigurableTypedSubQuery<P, PID, E, ID, R> : TypedSubQuery<P, PID, E, ID, R>
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID>,
        R: Any {

    fun limit(limit: Int, offset: Int = 0): ConfigurableTypedSubQuery<P, PID, E, ID, R>

    fun distinct(distinct: Boolean): ConfigurableTypedSubQuery<P, PID, E, ID, R>
}