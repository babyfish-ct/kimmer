package org.babyfish.kimmer.sql.spi

import org.babyfish.kimmer.sql.ast.SqlBuilder

interface Renderable {
    fun renderTo(builder: SqlBuilder)
}