package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.impl.SOURCE
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class AssociationSourcePropImpl(
    override val declaringType: AssociationType
) : AbstractAssociationPropImpl(declaringType) {

    override val name: String
        get() = SOURCE

    override val kotlinProp: KProperty1<*, *>
        get() = Association<*, *, *, *>::source

    override val returnType: KClass<*>
        get() = declaringType.sourceType.kotlinType

    override val javaReturnType: Class<*>
        get() = declaringType.sourceType.kotlinType.java

    override val targetType: ImmutableType
        get() = declaringType.sourceType

    override val elementType: KClass<*>
        get() = declaringType.sourceType.kotlinType
}