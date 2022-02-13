package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.selectable.SubSelectable
import org.babyfish.kimmer.sql.ast.table.NonNullSubQueryTable
import org.babyfish.kimmer.sql.ast.table.Table

interface MutableSubQuery<P, PID, E, ID> : MutableQuery<E, ID>, SubSelectable<P, PID, E, ID>
    where
        P: Entity<PID>,
        PID: Comparable<PID>,
        E: Entity<ID>,
        ID: Comparable<ID> {

    override val table: NonNullSubQueryTable<E, ID>

    val parentTable: Table<P, PID>
}
