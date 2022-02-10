package org.babyfish.kimmer.sql.ast

internal interface Renderable {
    fun renderTo(builder: SqlBuilder)
}