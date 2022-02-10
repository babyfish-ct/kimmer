package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.Immutable

interface TypedSqlSubQuery<P: Immutable, T: Immutable, R> : SqlSubQuery<P, T>, Expression<R>