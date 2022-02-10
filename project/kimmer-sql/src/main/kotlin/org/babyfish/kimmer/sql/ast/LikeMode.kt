package org.babyfish.kimmer.sql.ast

enum class LikeMode(
    val startExact: Boolean,
    val endExact: Boolean
) {
    EXACT(true, true),
    START(true, false),
    END(false, true),
    ANYWHERE(false, false)
}