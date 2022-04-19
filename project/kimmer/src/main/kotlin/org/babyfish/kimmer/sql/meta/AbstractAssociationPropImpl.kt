package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType

internal abstract class AbstractAssociationPropImpl(
    override val declaringType: AssociationType
) : ImmutableProp {

    abstract override val targetType: ImmutableType

    override val isNullable: Boolean
        get() = false

    override val isAssociation: Boolean
        get() = true

    override val isReference: Boolean
        get() = true

    override val isList: Boolean
        get() = false

    override val isConnection: Boolean
        get() = false

    override val isScalarList: Boolean
        get() = false

    override val isElementNullable: Boolean
        get() = false

    override fun toString(): String =
        kotlinProp.toString()
}