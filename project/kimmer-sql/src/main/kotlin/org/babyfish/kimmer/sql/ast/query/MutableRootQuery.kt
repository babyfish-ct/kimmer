package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.selectable.RootSelectable

interface MutableRootQuery<E, ID> : MutableQuery<E, ID>, RootSelectable<E, ID>
    where E:
          Entity<ID>,
          ID: Comparable<ID>