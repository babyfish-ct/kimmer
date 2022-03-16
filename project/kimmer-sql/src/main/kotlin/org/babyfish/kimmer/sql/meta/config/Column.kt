package org.babyfish.kimmer.sql.meta.config

data class Column(
    val name: String = "",
    val length: Int? = null,
    val precision: Int? = null,
    val scale: Int? = null,
    val onDelete: OnDeleteAction = OnDeleteAction.NONE
): Storage