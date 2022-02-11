package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class TableImpl<T: Immutable>(
    private val query: AbstractQueryImpl<*>,
    val entityType: EntityType,
    val parent: TableImpl<*>? = null,
    private val joinName: String? = null,
    val isInverse: Boolean = false,
    val joinProp: EntityProp? = null,
    var joinType: JoinType = JoinType.INNER
): JoinableTable<T>, Renderable {

    val alias: String

    val middleTableAlias: String?

    private var _used = parent === null

    private val childTableMap = mutableMapOf<String, TableImpl<*>>()

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

    override fun <X> get(prop: KProperty1<T, X?>): Expression<X> {
        val entityProp = entityType.props[prop.name] ?: error("No property '${prop.name}'")
        if (entityProp.targetType !== null) {
            throw IllegalArgumentException(
                "Can not get '${prop.name}' form table because it's association, " +
                    "please use joinReference, joinList or joinConnection"
            )
        }
        if (!entityProp.isId) {
            use()
        }
        return PropExpression(this, entityProp)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X : Immutable> joinReference(prop: KProperty1<T, X?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, joinType, false)
    }

    override fun <X : Immutable> joinList(prop: KProperty1<T, List<X>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, joinType, false)
    }

    override fun <X : Immutable> joinConnection(prop: KProperty1<T, Connection<X>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, joinType, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X : Immutable> reverseJoinReference(prop: KProperty1<X, T?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, joinType.reversed(), true)
    }

    override fun <X : Immutable> reverseJoinList(prop: KProperty1<X, List<T>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, joinType.reversed(), true)
    }

    override fun <X : Immutable> reverseJoinConnection(prop: KProperty1<X, Connection<T>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, joinType.reversed(), true)
    }

    private fun <X: Immutable> join0(
        entityProp: EntityProp,
        joinType: JoinType,
        inverse: Boolean
    ): JoinableTable<X> {
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

    private fun <X: Immutable> join1(
        joinName: String,
        inverse: Boolean,
        joinProp: EntityProp,
        joinType: JoinType
    ): JoinableTable<X> {
        val existing = childTableMap[joinName]
        if (existing !== null) {
            if (existing.joinType != joinType) {
                existing.joinType = JoinType.INNER
            }
            return existing as JoinableTable<X>
        }
        val newTable = TableImpl<X>(
            query,
            if (inverse) joinProp.declaringType else joinProp.targetType!!,
            this,
            joinName,
            inverse,
            joinProp,
            joinType
        )
        childTableMap[joinName] = newTable
        use()
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


    override fun renderTo(builder: SqlBuilder) {
        builder.renderSelf()
        if (_used) {
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
            if (_used) {
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
        } else if (_used) {
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
            if (_used) {
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

    private fun use() {
        if (!_used) {
            _used = true
            parent?.use()
        }
    }
}