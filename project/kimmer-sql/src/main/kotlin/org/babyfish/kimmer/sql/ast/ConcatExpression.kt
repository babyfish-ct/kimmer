package org.babyfish.kimmer.sql.ast

class ConcatContext internal constructor() {

    private val parts = mutableListOf<Expression<String>>()

    private var executionRequired = false

    private var terminated = false

    operator fun Expression<String>.unaryPlus() {
        if (terminated) {
            error("Current concat context has been terminated")
        }
        if (this !is ValueExpression) {
            executionRequired = true
        }
        parts += this
    }

    operator fun String.unaryPlus() {
        if (terminated) {
            error("Current concat context has been terminated")
        }
        parts += value(this)
    }

    internal fun createExpression(): Expression<String> {
        terminated = true
        return if (executionRequired) {
            ConcatExpression(parts)
        } else {
            val optimizedResult = parts.joinToString("") {
                (it as ValueExpression).value
            }
            value(optimizedResult)
        }
    }
}

internal class ConcatExpression(
    private val parts: List<Expression<String>>
): AbstractExpression<String>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        sql("concat")
        lowestPrecedence(true) {
            var sp: String? = null
            for (part in parts) {
                if (sp !== null) {
                    sql(sp)
                } else {
                    sp = ", "
                }
                render(part)
            }
        }
    }

    override fun accept(visitor: TableReferenceVisitor) {
        parts.forEach { it.accept(visitor) }
    }
}

