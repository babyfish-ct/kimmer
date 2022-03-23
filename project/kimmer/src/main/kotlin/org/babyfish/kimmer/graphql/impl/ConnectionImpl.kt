package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.UnloadedException
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.graphql.meta.ConnectionType
import org.babyfish.kimmer.runtime.ImmutableSpi

internal open class ConnectionImpl<N: Immutable>(
    type: ConnectionType
): ConnectionImplementor<N>(type), ImmutableSpi
{

    private var _totalCount: Int? = null

    private var _edges: List<Connection.Edge<N>>? = null

    private var _pageInfo: Connection.PageInfo? = null

    constructor(
        type: ConnectionType,
        copyFrom: ConnectionImplementor<N>
    ) : this(type) {
        _totalCount = if (copyFrom.`{loaded}`(TOTAL_COUNT)) copyFrom.totalCount else null
        _edges = if (copyFrom.`{loaded}`(EDGES)) copyFrom.edges else null
        _pageInfo = if (copyFrom.`{loaded}`(PAGE_INFO)) copyFrom.pageInfo else null
    }

    override val totalCount: Int
        get() = _totalCount ?: throw UnloadedException("totalCount is not loaded")

    override val edges: List<Connection.Edge<N>>
        get() = _edges ?: throw UnloadedException("'edges' is not loaded")

    override val pageInfo: Connection.PageInfo
        get() = _pageInfo ?: throw UnloadedException("'pageInfo' is not loaded")

    override fun `{loaded}`(prop: String): Boolean =
        when (prop) {
            TOTAL_COUNT -> _totalCount !== null
            EDGES -> _edges !== null
            PAGE_INFO -> _pageInfo !== null
            else -> throw IllegalArgumentException("No such prop '$prop'")
        }

    fun setTotalCount(value: Int?) {
        _totalCount = value
    }

    fun setEdges(value: List<Connection.Edge<N>>?) {
        _edges = value
    }

    fun setPageInfo(value: Connection.PageInfo?) {
        _pageInfo = value
    }
}