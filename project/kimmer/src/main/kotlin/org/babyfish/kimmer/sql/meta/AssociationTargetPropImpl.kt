package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Association
import org.babyfish.kimmer.sql.impl.TARGET
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class AssociationTargetPropImpl(
    override val declaringType: AssociationType
) : AbstractAssociationPropImpl(declaringType) {

    override val name: String
        get() = TARGET

    override val kotlinProp: KProperty1<*, *>
        get() = Association<*, *, *, *>::target

    override val returnType: KClass<*>
        get() = declaringType.targetType.kotlinType

    override val javaReturnType: Class<*>
        get() = declaringType.targetType.kotlinType.java

    override val targetType: ImmutableType
        get() = declaringType.targetType

    override val elementType: KClass<*>
        get() = declaringType.targetType.kotlinType
}