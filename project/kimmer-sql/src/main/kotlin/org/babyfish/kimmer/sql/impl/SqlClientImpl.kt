package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.ast.Executable
import org.babyfish.kimmer.sql.ast.MutableDelete
import org.babyfish.kimmer.sql.ast.MutableUpdate
import org.babyfish.kimmer.sql.ast.query.Queries
import org.babyfish.kimmer.sql.ast.query.impl.QueriesImpl
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.impl.AssociationEntityTypeImpl
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.runtime.R2dbcExecutor
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class SqlClientImpl(
    override val entityTypeMap: Map<KClass<out Entity<*>>, EntityType>,
    override val scalarProviderMap: Map<KClass<*>, ScalarProvider<*, *>>,
    override val dialect: Dialect,
    override val jdbcExecutor: JdbcExecutor,
    override val r2dbcExecutor: R2dbcExecutor
) : SqlClient {

    override val queries: Queries = QueriesImpl(this)

    override fun <E: Entity<ID>, ID: Comparable<ID>> createUpdate(
        type: KClass<E>,
        block: MutableUpdate<E, ID>.() -> Unit
    ): Executable<Int> =
        MutableUpdateImpl(this, validateType(type)).apply {
            block()
            freeze()
        }

    override fun <E: Entity<ID>, ID: Comparable<ID>> createDelete(
        type: KClass<E>,
        block: MutableDelete<E, ID>.() -> Unit
    ): Executable<Int> =
        MutableDeleteImpl(this, validateType(type)).apply {
            block()
            freeze()
        }

    override val entities: Entities =
        EntitiesImpl(this)

    override val associations: Associations =
        AssociationsImpl(this)

    internal fun entityTypeOf(type: KClass<*>): EntityType =
        entityTypeMap[type]
            ?: throw IllegalArgumentException(
                "Cannot get entity type from the unmapped type '${type.qualifiedName}'"
            )

    internal fun associationEntityTypeOf(
        prop: KProperty1<*, *>
    ): AssociationEntityTypeImpl {
        val ownerType = prop.parameters[0].type.classifier as KClass<*>?
            ?: throw IllegalArgumentException(
                "Cannot get association entity type because cannot extract owner type from '$prop'"
            )
        val ownerEntityType = entityTypeMap[ownerType]
            ?: throw IllegalArgumentException(
                "Cannot get association entity type base on property of unmapped type '$ownerType'"
            )
        val entityProp = ownerEntityType.props[prop.name]
            ?: throw IllegalArgumentException(
                "Cannot get association entity type because there is no entity property " +
                    "'${prop.name}' in the type '${ownerEntityType}'"
            )
        return (entityProp as EntityPropImpl).associationEntityType
            ?: throw IllegalArgumentException(
                "Cannot get association entity type because '$prop' is not base on middle table"
            )
    }

//    override val trigger: Trigger
//        get() = TODO("Not yet implemented")

    companion object {

        private fun <T: Any> validateType(type: KClass<T>): KClass<T> {
            if (Association::class.java.isAssignableFrom(type.java)) {
                throw IllegalArgumentException("Association type is not acceptable")
            }
            return type
        }
    }
}