package org.babyfish.kimmer.sql.meta.impl

import org.babyfish.kimmer.sql.meta.AssociationType
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.config.MiddleTable

internal class AssociationEntityTypeImpl(
    baseProp: EntityProp
): EntityType {

    override val immutableType: AssociationType

    override val tableName: String

    override val idProp: EntityProp

    val sourceProp: AssociationEntityPropImpl

    val targetProp: AssociationEntityPropImpl

    override val declaredProps: Map<String, EntityProp>

    override val starProps: Map<String, EntityProp>

    init {

        immutableType = AssociationType.of(
            baseProp.declaringType.kotlinType,
            baseProp.targetType!!.kotlinType
        )

        idProp = AssociationEntityPropImpl(
            this,
            immutableType.idProp,
            null,
            null
        )

        val mappedBy = baseProp.mappedBy
        if (mappedBy !== null) {
            val storage = mappedBy.storage
            if (storage !is MiddleTable) {
                throw IllegalArgumentException(
                    "Cannot not create AssociationEntityType base on '$baseProp', " +
                        "its inverse property '$mappedBy' is not base on middle table"
                )
            }
            tableName = storage.tableName
            sourceProp = AssociationEntityPropImpl(
                this,
                immutableType.sourceProp,
                baseProp.declaringType,
                Column(storage.targetJoinColumnName)
            )
            targetProp = AssociationEntityPropImpl(
                this,
                immutableType.targetProp,
                baseProp.targetType,
                Column(storage.joinColumnName)
            )
        } else {
            val storage = baseProp.storage
            if (storage !is MiddleTable) {
                throw IllegalArgumentException(
                    "Cannot not create AssociationEntityType base on '$baseProp', " +
                        "it is not base on middle table"
                )
            }
            tableName = storage.tableName
            sourceProp = AssociationEntityPropImpl(
                this,
                immutableType.sourceProp,
                baseProp.declaringType,
                Column(storage.joinColumnName)
            )
            targetProp = AssociationEntityPropImpl(
                this,
                immutableType.targetProp,
                baseProp.targetType,
                Column(storage.targetJoinColumnName)
            )
        }

        declaredProps = mapOf(
            idProp.name to idProp,
            sourceProp.name to sourceProp,
            targetProp.name to targetProp
        )

        starProps = mapOf(
            sourceProp.name to sourceProp,
            targetProp.name to targetProp
        )
    }

    override val superType: EntityType?
        get() = null

    override val derivedTypes: List<EntityType>
        get() = emptyList()

    override val idGenerator: IdGenerator?
        get() = null

    override val versionProp: EntityProp?
        get() = null

    override val props: Map<String, EntityProp>
        get() = declaredProps

    override val backProps: Set<EntityProp>
        get() = emptySet()

    override fun toString(): String =
        immutableType.toString()
}