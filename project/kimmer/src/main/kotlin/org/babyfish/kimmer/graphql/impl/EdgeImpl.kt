package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.UnloadedException
import org.babyfish.kimmer.graphql.meta.ConnectionEdgeType

internal class EdgeImpl<N: Immutable>(
    type: ConnectionEdgeType
) : EdgeImplementor<N>(type) {

    private var _node: N? = null

    private var _cursor: String? = null

    constructor(
        type: ConnectionEdgeType,
        copyFrom: EdgeImplementor<N>
    ): this(type) {
        _node = if (copyFrom.`{loaded}`(NODE)) copyFrom.node else null
        _cursor = if (copyFrom.`{loaded}`(CURSOR)) copyFrom.cursor else null
    }

    override val node: N
        get() = _node ?: throw UnloadedException("'node' is not loaded")

    override val cursor: String
        get() = _cursor ?: throw UnloadedException("'cursor' is not loaded")

    override fun `{loaded}`(prop: String): Boolean =
        when (prop) {
            NODE -> _node !== null
            CURSOR -> _cursor !== null
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }

    fun setNode(value: N?) {
        _node = value
    }

    fun setCursor(value: String?) {
        _cursor = value
    }
}