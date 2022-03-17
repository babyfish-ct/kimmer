package org.babyfish.kimmer.sql

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.runtime.MutationOptions
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import java.lang.IllegalStateException
import kotlin.reflect.KProperty1

@ScopedDSL
interface AbstractSaveOptionsDSL<E: Entity<*>> {

    fun keyProps(firstProp: KProperty1<E, *>, vararg moreProps: KProperty1<E, *>)

    fun <X: Entity<*>> reference(
        prop: KProperty1<E, X?>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    )

    fun <X: Entity<*>> list(
        prop: KProperty1<E, List<X>>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    )

    fun <X: Entity<*>> connection(
        prop: KProperty1<E, Connection<X>>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    )
}

interface SaveOptionsDSL<E: Entity<*>>: AbstractSaveOptionsDSL<E> {

    fun insertOnly()

    fun updateOnly()
}

interface AssociatedObjSaveOptionsDSL<E: Entity<*>>: AbstractSaveOptionsDSL<E> {

    fun createAttachedObjects()

    fun deleteDetachedObjects()
}

internal class SaveOptionsDSLImpl<E: Entity<*>>(
    private val entityType: EntityType,
    private var insertable: Boolean,
    private var updatable: Boolean,
    private var deletable: Boolean
) : SaveOptionsDSL<E>, AssociatedObjSaveOptionsDSL<E> {

    private var _keyProps: Set<EntityProp> = emptySet()

    private val childOptions = mutableMapOf<String, MutationOptions>()

    override fun keyProps(firstProp: KProperty1<E, *>, vararg moreProps: KProperty1<E, *>) {
        val props = mutableSetOf<KProperty1<E, *>>().apply {
            add(firstProp)
            for (moreProp in moreProps) {
                add(moreProp)
            }
        }
        _keyProps = mutableSetOf<EntityProp>().apply {
            for (prop in props) {
                val entityProp = entityType.props[prop.name]
                    ?: throw IllegalArgumentException(
                        "Illegal key property '${prop}', it's not declared by '${entityType.kotlinType.qualifiedName}'"
                    )
                if (entityProp.isReference || entityProp.isList || entityProp.isConnection) {
                    throw IllegalArgumentException(
                        "Illegal key property '${prop}', it's association"
                    )
                }
                if (entityProp.storage !is Column) {
                    throw IllegalArgumentException(
                        "Illegal key property '${prop}', it's storage is not simple column"
                    )
                }
                this += entityProp
            }
        }
    }

    override fun <X : Entity<*>> reference(
        prop: KProperty1<E, X?>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    ) {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("Illegal reference property '${entityProp}'")
        }
        addChildOptions(entityProp, block)
    }

    override fun <X : Entity<*>> list(
        prop: KProperty1<E, List<X>>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    ) {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("Illegal reference property '${entityProp}'")
        }
        addChildOptions(entityProp, block)
    }

    override fun <X : Entity<*>> connection(
        prop: KProperty1<E, Connection<X>>,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    ) {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("Illegal reference property '${entityProp}'")
        }
        addChildOptions(entityProp, block)
    }

    override fun insertOnly() {
        if (!insertable) {
            throw IllegalStateException("Cannot invoke both insertOnly() and updateOnly()")
        }
        updatable = false
    }

    override fun updateOnly() {
        if (!updatable) {
            throw IllegalStateException("Cannot invoke both insertOnly() and updateOnly()")
        }
        insertable = false
    }

    override fun createAttachedObjects() {
        insertable = true
    }

    override fun deleteDetachedObjects() {
        deletable = true
    }

    private fun <X: Entity<*>> addChildOptions(
        entityProp: EntityProp,
        block: AssociatedObjSaveOptionsDSL<X>.() -> Unit
    ) {
        if (childOptions.containsKey(entityProp.name)) {
            throw IllegalArgumentException("Child save options of association '${entityProp}' has been configured")
        }
        val childOption = SaveOptionsDSLImpl<X>(
            entityProp.targetType!!,
            insertable = false,
            updatable = true,
            deletable = false
        ).run {
            block()
            build()
        }
        childOptions[entityProp.name] = childOption
    }

    internal fun build(): MutationOptions =
        MutationOptions(
            entityType,
            insertable,
            updatable,
            deletable,
            _keyProps.takeIf { it.isNotEmpty() },
            childOptions
        )
}
