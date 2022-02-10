package org.babyfish.kimmer.sql.meta.config

data class MiddleTable (
    val tableName: String,
    val joinColumnName: String,
    val targetJoinColumnName: String
): Storage