package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType

internal data class MutationOptions(
    val entityType: EntityType,
    val insertable: Boolean,
    val updatable: Boolean,
    val deletable: Boolean,
    val keyProps: Set<EntityProp>?,
    val targetMutationOptions: MutableMap<String, MutationOptions>
)