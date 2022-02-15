package org.babyfish.kimmer.sql.meta.spi

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.meta.EntityType
import kotlin.reflect.KProperty1

interface MetaFactory {

    fun createEntityType(
        immutableType: ImmutableType
    ): EntityTypeImpl

    fun createEntityProp(
        declaringType: EntityType,
        kotlinProp: KProperty1<*, *>
    ): EntityPropImpl
}

internal object DefaultMetaFactory: MetaFactory {

    override fun createEntityType(
        immutableType: ImmutableType
    ): EntityTypeImpl =
        EntityTypeImpl(this, immutableType)

    override fun createEntityProp(
        declaringType: EntityType,
        kotlinProp: KProperty1<*, *>
    ): EntityPropImpl =
        EntityPropImpl(declaringType, kotlinProp)
}