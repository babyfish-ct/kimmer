package org.babyfish.kimmer.sql.ast.query

import org.babyfish.kimmer.sql.ast.Executable

interface TypedRootQuery<R>: Executable<List<R>> {

    infix fun union(right: TypedRootQuery<R>): TypedRootQuery<R>

    infix fun unionAll(right: TypedRootQuery<R>): TypedRootQuery<R>

    infix fun minus(right: TypedRootQuery<R>): TypedRootQuery<R>

    infix fun intersect(right: TypedRootQuery<R>): TypedRootQuery<R>
}
