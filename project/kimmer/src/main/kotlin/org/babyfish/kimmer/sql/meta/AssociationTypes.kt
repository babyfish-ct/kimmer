package org.babyfish.kimmer.sql.meta

import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableProp
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface AssociationType: ImmutableType {
    val sourceType: ImmutableType
    val targetType: ImmutableType
    val idProp: ImmutableProp
    val sourceProp: ImmutableProp
    val targetProp: ImmutableProp

    companion object {

        @JvmStatic
        fun of(
            sourceType: KClass<out Entity<*>>,
            targetType: KClass<out Entity<*>>,
        ): AssociationType {
            val key = sourceType to targetType
            return associationTypeLock.read {
                associationTypes[key]
            } ?: associationTypeLock.write {
                associationTypes[key]
                    ?: AssociationTypeImpl(
                        ImmutableType.of(key.first),
                        ImmutableType.of(key.second)
                    ).also {
                        associationTypes[key] = it
                    }
            }
        }

        private val associationTypes =
            mutableMapOf<Pair<KClass<out Immutable>, KClass<out Immutable>>, AssociationTypeImpl>()

        private val associationTypeLock =
            ReentrantReadWriteLock()
    }
}
