package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.jackson.immutableObjectMapper

internal const val ID = "id"
internal const val SOURCE = "source"
internal const val TARGET = "target"

internal val IMMUTABLE_MAPPER = immutableObjectMapper()

internal const val JACKSON_GENERIC_SOURCE_TYPE = "__genericSourceType"

internal const val JACKSON_GENERIC_TARGET_TYPE = "__genericTargetType"