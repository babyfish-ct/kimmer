package org.babyfish.kimmer.sql.ast

enum class JoinType {
    INNER,
    LEFT {
        override fun reversed(): JoinType = RIGHT
    },
    RIGHT {
        override fun reversed(): JoinType = LEFT
    },
    FULL;

    open fun reversed(): JoinType = this
}