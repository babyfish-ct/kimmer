package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.MutationType
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.JdbcSqlBuilder
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class JdbcDeleter(
    private val sqlClient: SqlClient,
    private val con: Connection
) {
    fun delete(
        ids: Collection<Any>,
        mutationOptions: MutationOptions
    ): List<MutationContext> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        val contexts = ids.map {
            MutationContext(it, mutationOptions)
        }
        delete(contexts)
        return contexts
    }

    @Suppress("UNCHECKED_CAST")
    fun delete(
        contexts: Collection<MutationContext>
    ) {
        if (contexts.isEmpty()) {
            return
        }
        val mutationOptions = contexts.first().mutationOptions
        val entityType = mutationOptions.entityType
        contexts
            .filter { !it.isEntityInitialized }
            .takeIf { it.isNotEmpty() }
            ?.let { noEntityContexts ->
                val entities = sqlClient
                    .createQuery(entityType.kotlinType as KClass<Entity<FakeId>>) {
                        where {
                            table.get(
                                entityType.idProp.kotlinProp as KProperty1<Entity<FakeId>, Any>
                            ) valueIn noEntityContexts.map { it.entityId }
                        }
                        select(table)
                    }
                    .execute(con) as List<Entity<*>>
                val entityMap = entities.associateBy { it.id }
                for (ctx in noEntityContexts) {
                    val entity = entityMap[ctx.entityId]
                    if (entity !== null) {
                        ctx.entity = entity
                    }
                }
            }
        contexts
            .filter { it.isEntityInitialized }
            .takeIf { it.isNotEmpty() }
            ?.let { entityContexts ->
                deleteAssociations(entityContexts, mutationOptions)
                val (sql, variables) = JdbcSqlBuilder(sqlClient)
                    .apply {
                        sql("delete from ")
                        sql(entityType.tableName)
                        sql(" where ")
                        sql((entityType.idProp.storage as Column).name)
                        sql(" in (")
                        entityContexts.forEachIndexed { index, ctx ->
                            if (index != 0) {
                                sql(", ")
                            }
                            variable(ctx.entityId)
                        }
                        sql(")")
                    }
                    .build()
                sqlClient.jdbcExecutor.execute(con, sql, variables) {
                    executeUpdate()
                }
                entityContexts.forEach {
                    it.type = MutationType.DELETE
                }
            }
        contexts
            .filter { !it.isEntityInitialized }
            .forEach {
                it.entity = produce(entityType.kotlinType) {
                    Draft.set(this, entityType.idProp.immutableProp, it.entityId)
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun deleteAssociations(
        contexts: List<MutationContext>,
        mutationOptions: MutationOptions
    ) {
        val entityType = mutationOptions.entityType
        for (prop in entityType.props.values) {
            val targetType = prop.targetType ?: continue
            val mappedBy = prop.mappedBy
            if (mappedBy?.isReference == true) {
                val childGroupMap = sqlClient.createQuery(targetType.kotlinType as KClass<Entity<FakeId>>) {
                    val parentId = table
                        .joinReference(
                            mappedBy.kotlinProp as KProperty1<Entity<FakeId>, Entity<FakeId>>
                        )
                        .get(
                            entityType.idProp.kotlinProp as KProperty1<Entity<FakeId>, Any>
                        )
                    where(parentId valueIn contexts.map { it.entityId })
                    select {
                        parentId then table
                    }
                }.execute(con).groupBy({ it.first}) {
                    it.second
                }
                if (childGroupMap.isNotEmpty()) {
                    for (ctx in contexts) {
                        val targets = childGroupMap[ctx.entityId] ?: continue
                        ctx.deleteAssociation(prop) {
                            detachByTargets(targets)
                            handleChildTable(this)
                        }
                    }
                }
            } else {
                val middleTable = (prop.storage as? MiddleTable)
                    ?: prop.mappedBy?.storage as? MiddleTable
                if (middleTable !== null) {
                    val (tableName, joinColumnName, targetJoinColumnName) =
                        if (prop.storage is MiddleTable) {
                            Triple(middleTable.tableName, middleTable.joinColumnName, middleTable.targetJoinColumnName)
                        } else {
                            Triple(middleTable.tableName, middleTable.targetJoinColumnName, middleTable.joinColumnName)
                        }
                    val (sql, variables) = JdbcSqlBuilder(sqlClient)
                        .apply {
                            sql("select ")
                            sql(joinColumnName)
                            sql(", ")
                            sql(targetJoinColumnName)
                            sql(" from ")
                            sql(tableName)
                            sql(" where ")
                            sql(joinColumnName)
                            sql(" in (")
                            contexts.forEachIndexed { index, ctx ->
                                if (index != 0) {
                                    sql(", ")
                                }
                                variable(ctx.entityId)
                            }
                            sql(")")
                        }
                        .build()
                    val childGroupMap = sqlClient.jdbcExecutor.execute(con, sql, variables) {
                        executeQuery().use {
                            mutableListOf<Pair<Any, Any>>().apply {
                                while (it.next()) {
                                    this += it.getObject(JDBC_BASE_INDEX) to
                                        it.getObject(JDBC_BASE_INDEX + 1)
                                }
                            }
                        }
                    }.groupBy({it.first}) {
                        it.second
                    }
                    if (childGroupMap.isNotEmpty()) {
                        for (ctx in contexts) {
                            val targetIds = childGroupMap[ctx.entityId] ?: continue
                            ctx.deleteAssociation(prop) {
                                detachByTargetIds(targetIds)
                                handleMiddleTable(this, tableName, joinColumnName)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleChildTable(
        ctx: MutationContext.AssociationContext
    ) {
        val childType = ctx.entityProp.targetType!!
        val parentProp = ctx.entityProp.mappedBy!!
        val fkColumn = parentProp.storage as Column
        if (fkColumn.onDelete == OnDeleteAction.NONE) {
            throw ExecutionException(
                "Cannot delete the entity '${ctx.owner.entity}', " +
                    "the 'onDelete' of parent property '${parentProp}' is 'NONE' " +
                    "but there are some child objects whose type is '${childType.kotlinType.qualifiedName}': " +
                    ctx.detachedTargets.toLimitString { it.entity.toString() }
            )
        }
        if (fkColumn.onDelete == OnDeleteAction.SET_NULL) {
            val (sql, variables) = JdbcSqlBuilder(sqlClient)
                .apply {
                    sql("update ")
                    sql(childType.tableName)
                    sql(" set ")
                    sql(fkColumn.name)
                    sql(" = null where ")
                    sql(fkColumn.name)
                    sql(" = ")
                    variable(ctx.owner.entityId)
                }.build()
            sqlClient.jdbcExecutor.execute(con, sql, variables) {
                if (executeUpdate() != ctx.detachedTargets.size) {
                    throw ExecutionException("Concurrent modification error")
                }
            }
            ctx.detachedTargets.forEach {
                it.type = MutationType.UPDATE
            }
            return
        }
        delete(ctx.detachedTargets)
    }

    private fun handleMiddleTable(
        ctx: MutationContext.AssociationContext,
        tableName: String,
        joinColumnName: String
    ) {
        val (sql, variables) = JdbcSqlBuilder(sqlClient)
            .apply {
                sql("delete from ")
                sql(tableName)
                sql(" where ")
                sql(joinColumnName)
                sql(" = ")
                variable(ctx.owner.entityId)
            }
            .build()
        sqlClient.jdbcExecutor.execute(con, sql, variables) {
            if (executeUpdate() != ctx.detachedTargets.size) {
                throw ExecutionException("Concurrent modification error")
            }
        }
        ctx.detachedTargets.forEach {
            it.middleTableDeleted()
        }
    }
}