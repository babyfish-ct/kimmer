package org.babyfish.kimmer.sql.ast

interface Ast {
    fun accept(visitor: AstVisitor)
}
