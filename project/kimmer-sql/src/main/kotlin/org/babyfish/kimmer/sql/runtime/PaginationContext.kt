package org.babyfish.kimmer.sql.runtime

class PaginationContext internal constructor(
    val limit: Int,
    val offset: Int,
    private val originSql: String,
    private val originVariables: List<Any>,
    private val r2dbc: Boolean,
) {
    private val builder = StringBuilder()

    private val variables = mutableListOf<Any>()

    private var containsOrigin = false

    fun origin() {
        if (containsOrigin) {
            error("origin() can only be called once")
        }
        builder.append(originSql)
        variables.addAll(originVariables)
        containsOrigin = true
    }

    fun sql(sql: String) {
        builder.append(sql)
    }

    fun variable(value: Any) {
        if (!containsOrigin) {
            error("Cannot add variables before the origin() is called")
        }
        variables += value
        val sql = if (r2dbc) {
            "$${variables.size}"
        } else {
            "?"
        }
        builder.append(sql)
    }

    internal fun build(): Pair<String, List<Any>> {
        if (!containsOrigin) {
            error("origin() is has not been called")
        }
        return builder.toString() to variables
    }
}