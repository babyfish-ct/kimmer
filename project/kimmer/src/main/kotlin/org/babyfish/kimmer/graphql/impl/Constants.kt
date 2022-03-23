package org.babyfish.kimmer.graphql.impl

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.jackson.immutableObjectMapper
import org.babyfish.kimmer.produce

internal const val TOTAL_COUNT = "totalCount"
internal const val EDGES = "edges"
internal const val PAGE_INFO = "pageInfo"

internal const val NODE = "node"
internal const val CURSOR = "cursor"

internal const val JACKSON_GENERIC_TYPE = "__genericType"

internal val EMPTY_PAGE_INFO = produce(Connection.PageInfo::class) {}

internal val IMMUTABLE_MAPPER = immutableObjectMapper()