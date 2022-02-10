package org.babyfish.kimmer.sql.ast

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import kotlin.reflect.KProperty1

internal class TableImpl<T: Immutable>(
    private val query: AbstractQueryImpl<*>,
    val entityType: EntityType,
    val parent: TableImpl<*>? = null,
    val parentProp: EntityProp? = null,
    var joinType: JoinType = JoinType.INNER
): JoinableTable<T>, Renderable {

    val alias: String

    val middleTableAlias: String?

    private var _used = parent === null

    init {
        if ((parent === null) != (parentProp === null)) {
            error("Internal bug: Bad constructor arguments for TableImpl")
        }
        middleTableAlias = (parentProp?.mappedBy?: parentProp)
            ?.storage
            ?.let {
                (it as? MiddleTable)?.let {
                    query.tableAliasAllocator.allocate()
                }
            }
        alias = query.tableAliasAllocator.allocate()
    }

    private val childTableMap = mutableMapOf<String, TableImpl<*>>()

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
        return join(entityProp, joinType)
    }

    override fun <X : Immutable> joinList(prop: KProperty1<T, List<X>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join(entityProp, joinType)
    }

    override fun <X : Immutable> joinConnection(prop: KProperty1<T, Connection<X>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join(entityProp, joinType)
    }

    private fun <X: Immutable> join(entityProp: EntityProp, joinType: JoinType): JoinableTable<X> {
        val existing = childTableMap[entityProp.name]
        if (existing !== null) {
            if (existing.joinType != joinType) {
                existing.joinType = JoinType.INNER
            }
            return existing as JoinableTable<X>
        }
        val newTable = TableImpl<X>(query, entityProp.targetType!!, this, entityProp, joinType)
        childTableMap[entityProp.name] = newTable
        use()
        return newTable
    }

    override fun renderTo(builder: SqlBuilder) {
        builder.renderSelf()
        if (_used) {
            for (childTable in childTableMap.values) {
                childTable.renderTo(builder)
            }
        }
    }

    private fun SqlBuilder.renderSelf() {
        if (parentProp?.mappedBy !== null) {
            inverseJoin()
        } else if (parentProp !== null) {
            join()
        } else {
            sql(" from ")
            sql(entityType.tableName)
            sql(" as ")
            sql(alias)
        }
    }

    private fun SqlBuilder.join() {

        val parent = parent!!
        val prop = parentProp!!
        val middleTable = prop.storage as? MiddleTable

        if (middleTable !== null) {
            joinImpl(
                joinType,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.joinColumnName
            )
            if (_used) {
                joinImpl(
                    joinType,
                    middleTableAlias!!,
                    middleTable.targetJoinColumnName,
                    entityType.tableName,
                    alias,
                    (entityType.idProp.storage as Column).name
                )
            }
        } else if (_used) {
            joinImpl(
                joinType,
                parent.alias,
                (prop.storage as Column).name,
                entityType.tableName,
                alias,
                (parent.entityType.idProp.storage as Column).name
            )
        }
    }

    private fun SqlBuilder.inverseJoin() {

        val parent = parent!!
        val inverseProp = parentProp?.mappedBy!!
        val middleTable = inverseProp.storage as? MiddleTable

        if (middleTable !== null) {
            joinImpl(
                joinType,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.targetJoinColumnName
            )
            if (_used) {
                joinImpl(
                    joinType,
                    middleTableAlias!!,
                    middleTable.joinColumnName,
                    entityType.tableName,
                    alias,
                    (entityType.idProp.storage as Column).name
                )
            }
        } else { // One-to-many join cannot be optimized by "_used"
            joinImpl(
                joinType,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                entityType.tableName,
                alias,
                (inverseProp.storage as Column).name
            )
        }
    }

    private fun SqlBuilder.joinImpl(
        joinType: JoinType,
        previousAlias: String,
        previousColumnName: String,
        newTableName: String,
        newAlias: String,
        newColumnName: String
    ) {
        sql(" ")
        sql(joinType.name.lowercase())
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