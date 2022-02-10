package org.babyfish.kimmer.sql.meta.impl

internal enum class ResolvingPhase {
    SUPER_TYPE,
    DECLARED_PROPS,
    PROPS,
    PROP_TARGET,
    PROP_MAPPED_BY,
    PROP_DEFAULT_COLUMN,
    ID_PROP,
}