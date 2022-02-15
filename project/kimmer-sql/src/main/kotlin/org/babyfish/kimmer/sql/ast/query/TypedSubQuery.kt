package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.Selection

interface TypedSubQuery<P, PID, E, ID, R> : Expression<R>, Selection<R>
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID>,
        R: Any {

    infix fun union(
            right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R>

    infix fun unionAll(
            right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R>

    infix fun minus(
            right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R>

    infix fun intersect(
            right: TypedSubQuery<P, PID, E, ID, R>
    ): TypedSubQuery<P, PID, E, ID, R>
}