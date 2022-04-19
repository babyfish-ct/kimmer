package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.config.Storage
import kotlin.reflect.KClass

internal class AssociationEntityPropImpl(
    override val declaringType: AssociationEntityTypeImpl,
    override val immutableProp: ImmutableProp,
    override val targetType: EntityType?,
    override val storage: Storage?
): EntityProp {

    init {
        if ((targetType !== null) != (storage !== null)) {
            error("Internal bug: Error arguments for association entity property")
        }
    }

    override val isId: Boolean
        get() = targetType === null

    override val isVersion: Boolean
        get() = false

    override val isTransient: Boolean
        get() = false

    override val returnType: KClass<*>
        get() = immutableProp.returnType

    override val javaReturnType: Class<*>
        get() = immutableProp.javaReturnType

    override val isReference: Boolean
        get() = storage !== null

    override val isList: Boolean
        get() = false

    override val isConnection: Boolean
        get() = false

    override val isAssociation: Boolean
        get() = targetType !== null

    override val isNullable: Boolean
        get() = false

    override val isTargetNullable: Boolean
        get() = false

    override val mappedBy: EntityProp?
        get() = null

    override val opposite: EntityProp?
        get() = null

    override val scalarProvider: ScalarProvider<*, *>?
        get() = targetType?.idProp?.scalarProvider
}