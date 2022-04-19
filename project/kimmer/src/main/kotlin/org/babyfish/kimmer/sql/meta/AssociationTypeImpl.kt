package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.meta.ScalarPropImpl
import org.babyfish.kimmer.meta.DraftInfo
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Association
import kotlin.reflect.KClass

internal class AssociationTypeImpl(
    override val sourceType: ImmutableType,
    override val targetType: ImmutableType
): AssociationType {

    init {
        if (Association::class.java.isAssignableFrom(sourceType.kotlinType.java)) {
            throw IllegalArgumentException("Association.source cannot be association type")
        }
        if (Association::class.java.isAssignableFrom(targetType.kotlinType.java)) {
            throw IllegalArgumentException("Association.target cannot be association type")
        }
    }

    override val idProp: ImmutableProp = ScalarPropImpl(this, Association<*, *, *, *>::id)

    override val sourceProp: ImmutableProp = AssociationSourcePropImpl(this)

    override val targetProp: ImmutableProp = AssociationTargetPropImpl(this)

    override val kotlinType: KClass<out Immutable>
        get() = Association::class

    override val simpleName: String
        get() = Association::class.simpleName!!

    override val qualifiedName: String
        get() = Association::class.qualifiedName!!

    override val isAbstract: Boolean
        get() = false

    override val superTypes: Set<ImmutableType>
        get() = emptySet()

    override val declaredProps: Map<String, ImmutableProp> =
        mapOf(
            "id" to idProp,
            "source" to sourceProp,
            "target" to targetProp
        )

    override val props: Map<String, ImmutableProp>
        get() = declaredProps

    override val draftInfo: DraftInfo?
        get() = null

    override fun toString(): String =
        "Association<${sourceType.qualifiedName},,${targetType.qualifiedName},>"
}