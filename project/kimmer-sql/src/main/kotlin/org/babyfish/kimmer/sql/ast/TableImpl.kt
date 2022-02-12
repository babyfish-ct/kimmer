package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal open class TableImpl<E: Entity<ID>, ID: Comparable<ID>>(
    private val query: AbstractQueryImpl<*, *>,
    val entityType: EntityType,
    val parent: TableImpl<*, *>? = null,
    val isInverse: Boolean = false,
    val joinProp: EntityProp? = null,
    private var joinType: JoinType = JoinType.INNER
): JoinableTable<E, ID>, Renderable {

    val alias: String

    val middleTableAlias: String?

    private var _staticallyUsed = parent === null

    private val childTableMap = mutableMapOf<String, TableImpl<*, *>>()

    init {
        if ((parent === null) != (joinProp === null)) {
            error("Internal bug: Bad constructor arguments for TableImpl")
        }
        middleTableAlias = joinProp
            ?.storage
            ?.let {
                (it as? MiddleTable)?.let {
                    query.tableAliasAllocator.allocate()
                }
            }
        alias = query.tableAliasAllocator.allocate()
    }

    protected open fun <X: Entity<XID>, XID: Comparable<XID>> createChildTable(
        query: AbstractQueryImpl<*, *>,
        entityType: EntityType,
        isInverse: Boolean,
        joinProp: EntityProp,
        joinType: JoinType
    ): TableImpl<X, XID> =
        TableImpl(
            query,
            if (isInverse) joinProp.declaringType else joinProp.targetType!!,
            this,
            isInverse,
            joinProp,
            joinType
        )

    override val id: Expression<ID>
        get() =
            PropExpression(this, entityType.idProp)

    override fun <X> get(prop: KProperty1<E, X?>): Expression<X> {
        val entityProp = entityType.props[prop.name] ?: error("No property '${prop.name}'")
        if (!entityProp.isId) {
            staticallyUse()
        }
        return PropExpression(this, entityProp)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X: Entity<XID>, XID: Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, joinType, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>?>,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, joinType, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>?>,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, joinType, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, joinType.reversed(), true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>?>,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, joinType.reversed(), true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>?>,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, joinType.reversed(), true)
    }

    private fun <X: Entity<XID>, XID: Comparable<XID>> join0(
        entityProp: EntityProp,
        joinType: JoinType,
        inverse: Boolean
    ): JoinableTable<X, XID> {
        val joinName = if (!inverse) {
            entityProp.name
        } else {
            entityProp?.opposite?.name
                ?: "inverse(${entityProp.kotlinProp})"
        }
        return if (entityProp.mappedBy !== null) {
            join1(joinName, !inverse, entityProp.mappedBy!!, joinType.reversed())
        } else {
            join1(joinName, inverse, entityProp, joinType)
        }
    }

    private fun <X: Entity<XID>, XID: Comparable<XID>> join1(
        joinName: String,
        inverse: Boolean,
        joinProp: EntityProp,
        joinType: JoinType
    ): JoinableTable<X, XID> {
        val existing = childTableMap[joinName]
        if (existing !== null) {
            if (existing.joinType != joinType) {
                existing.joinType = JoinType.INNER
            }
            return existing as JoinableTable<X, XID>
        }
        val newTable = this.createChildTable<X, XID>(
            query,
            if (inverse) joinProp.declaringType else joinProp.targetType!!,
            inverse,
            joinProp,
            joinType
        )
        childTableMap[joinName] = newTable
        staticallyUse()
        return newTable
    }

    private fun reverseType(prop: KProperty1<*, *>): EntityType =
        prop
            .parameters
            .takeIf {
                it.size == 1
            }
            ?.map { it.type.classifier }
            ?.filterIsInstance<KClass<*>>()
            ?.firstOrNull()
            ?.let {
                query.sqlClient.entityTypeMap[it]
            } ?: error("The declaring type of reversed prop is not mapped entity")

    override fun <X: Entity<XID>, XID: Comparable<XID>> listContains(
        prop: KProperty1<E, List<X>?>,
        xIds: Collection<XID>
    ): Expression<Boolean> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return contains0(entityProp, xIds, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> connectionContains(
        prop: KProperty1<E, Connection<X>?>,
        xIds: Collection<XID>
    ): Expression<Boolean> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return contains0(entityProp, xIds, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←listContains`(
        prop: KProperty1<X, List<E>?>,
        xIds: Collection<XID>
    ): Expression<Boolean> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return contains0(entityProp, xIds, true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←connectionContains`(
        prop: KProperty1<X, Connection<E>?>,
        xIds: Collection<XID>
    ): Expression<Boolean> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return contains0(entityProp, xIds, true)
    }

    private fun contains0(
        prop: EntityProp,
        xIds: Collection<Any>,
        inverse: Boolean
    ): Expression<Boolean> =
        if (prop.mappedBy !== null) {
            ContainsExpression(this, prop.mappedBy!!, xIds, !inverse)
        } else {
            ContainsExpression(this, prop, xIds, inverse)
        }

    override fun renderTo(builder: SqlBuilder) {
        builder.renderSelf()
        if (isUsedBy(builder)) {
            for (childTable in childTableMap.values) {
                childTable.renderTo(builder)
            }
        }
    }

    private fun SqlBuilder.renderSelf() {
        if (isInverse) {
            renderInverseJoin()
        } else if (joinProp !== null) {
            renderJoin()
        } else {
            sql(" from ")
            sql(entityType.tableName)
            sql(" as ")
            sql(alias)
        }
    }

    private fun SqlBuilder.renderJoin() {

        val parent = parent!!
        val middleTable = joinProp!!.storage as? MiddleTable

        if (middleTable !== null) {
            joinImpl(
                joinType,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.joinColumnName,
                false
            )
            if (isUsedBy(this)) {
                joinImpl(
                    joinType,
                    middleTableAlias!!,
                    middleTable.targetJoinColumnName,
                    entityType.tableName,
                    alias,
                    (entityType.idProp.storage as Column).name,
                    false
                )
            }
        } else if (isUsedBy(this)) {
            joinImpl(
                joinType,
                parent.alias,
                (joinProp.storage as Column).name,
                entityType.tableName,
                alias,
                (parent.entityType.idProp.storage as Column).name,
                false
            )
        }
    }

    private fun SqlBuilder.renderInverseJoin() {

        val parent = parent!!
        val middleTable = joinProp!!.storage as? MiddleTable

        if (middleTable !== null) {
            joinImpl(
                joinType,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.targetJoinColumnName,
                true
            )
            if (isUsedBy(this)) {
                joinImpl(
                    joinType,
                    middleTableAlias!!,
                    middleTable.joinColumnName,
                    entityType.tableName,
                    alias,
                    (entityType.idProp.storage as Column).name,
                    true
                )
            }
        } else { // One-to-many join cannot be optimized by "_used"
            joinImpl(
                joinType,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                entityType.tableName,
                alias,
                (joinProp.storage as Column).name,
                true
            )
        }
    }

    private fun SqlBuilder.joinImpl(
        joinType: JoinType,
        previousAlias: String,
        previousColumnName: String,
        newTableName: String,
        newAlias: String,
        newColumnName: String,
        inverse: Boolean
    ) {
        val jt =
            if (inverse) {
                joinType.reversed()
            } else {
                joinType
            }
        sql(" ")
        sql(jt.name.lowercase())
        sql(" join ")
        sql(newTableName)
        sql(" as ")
        sql(newAlias)
        sql(" on ")
        sql(previousAlias)
        sql(".")
        sql(previousColumnName)
        sql(" = ")
        sql(newAlias)
        sql(".")
        sql(newColumnName)
    }

    private fun staticallyUse() {
        if (!_staticallyUsed) {
            _staticallyUsed = true
            parent?.staticallyUse()
        }
    }

    private fun isUsedBy(builder: SqlBuilder): Boolean =
        parent === null || (_staticallyUsed && builder.isTableUsed(this))

    fun renderAsSelection(builder: SqlBuilder) {
        var sp: String? = null
        for (prop in entityType.starProps.values) {
            if (sp === null) {
                sp = ", "
            } else {
                builder.sql(sp)
            }
            renderSelection(prop, builder, true)
        }
    }

    fun renderSelection(prop: EntityProp, builder: SqlBuilder, starMode: Boolean) {
        builder.apply {
            if (prop.isId && joinProp !== null) {
                val middleTable = joinProp.storage as? MiddleTable
                val inverse = isInverse
                if (middleTable !== null) {
                    sql(middleTableAlias!!)
                    sql(".")
                    sql(
                        if (inverse) {
                            middleTable.joinColumnName
                        } else {
                            middleTable.targetJoinColumnName
                        }
                    )
                    return
                }
                if (!inverse) {
                    sql(parent!!.alias)
                    sql(".")
                    sql((joinProp.storage as Column).name)
                    return
                }
            }
            val storage = prop.storage
            if (storage is Formula) {
                resolveFormula(prop) {
                    storage.get(this@TableImpl).let {
                        if (starMode && it is SqlSubQuery<*, *, *, *>) {
                            throw MappingException(
                                "Cannot select formula prop implicitly (select whole table) because it's expensive"
                            )
                        }
                        (it as Renderable).renderTo(builder)
                    }
                }
            } else {
                sql(alias)
                sql(".")
                sql((storage as Column).name)
            }
        }
    }

    val destructive: Destructive
        get() {
            if (joinProp === null) {
                return Destructive.NONE
            }
            val (prop, jt) =
                if (isInverse) {
                    (joinProp.opposite ?: return Destructive.BREAK_REPEATABILITY) to
                        joinType.reversed()
                } else {
                    joinProp to joinType
                }
            if (prop.isList || prop.isConnection) {
                return Destructive.BREAK_REPEATABILITY
            }
            if (prop.isNullable && jt == JoinType.LEFT) {
                return Destructive.NONE
            }
            if (!prop.isNullable && (jt == JoinType.LEFT || jt == JoinType.INNER)) {
                return Destructive.NONE
            }
            return Destructive.BREAK_ROW_COUNT
        }

    enum class Destructive {
        NONE, // Left join for nullable reference, Left/Inner join for non-null reference
        BREAK_ROW_COUNT, // inner join for nullable-reference
        BREAK_REPEATABILITY // Any join for Collection
    }
}