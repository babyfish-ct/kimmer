package org.babyfish.kimmer.sql.runtime

import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.MutationType
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.R2dbcSqlBuilder
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction
import io.r2dbc.spi.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class R2dbcDeleter(
    private val sqlClient: SqlClient,
    private val con: Connection
) {
    suspend fun delete(
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
    suspend fun delete(
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
                val (sql, variables) = R2dbcSqlBuilder(sqlClient)
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
                sqlClient.r2dbcExecutor.execute(con, sql, variables) {
                    rowsUpdated.awaitSingle()
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
    private suspend fun deleteAssociations(
        contexts: List<MutationContext>,
        mutationOptions: MutationOptions
    ) {
        val entityType = mutationOptions.entityType
        for (prop in entityType.props.values) {
            val storage = prop.storage as? MiddleTable ?: continue
            handleMiddleTable(
                contexts,
                prop,
                null,
                storage.tableName,
                storage.joinColumnName,
                storage.targetJoinColumnName
            )
        }
        for (backProp in entityType.backProps) {
            val targetType = backProp.declaringType
            val backStorage = backProp.storage
            if (backStorage is Column) {
                val childGroupMap = sqlClient.createQuery(targetType.kotlinType as KClass<Entity<FakeId>>) {
                    val parentId = table
                        .joinReference(
                            backProp.kotlinProp as KProperty1<Entity<FakeId>, Entity<FakeId>>
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
                        ctx.deleteAssociationByBackPropAsync(backProp) {
                            detachByTargets(targets)
                            handleChildTable(this)
                        }
                    }
                }
            } else if (backStorage is MiddleTable) {
                handleMiddleTable(
                    contexts,
                    null,
                    backProp,
                    backStorage.tableName,
                    backStorage.targetJoinColumnName,
                    backStorage.joinColumnName
                )
            }
        }
    }

    private suspend fun handleChildTable(
        ctx: MutationContext.AssociationContext
    ) {
        val childType = ctx.targetType
        val backProp = ctx.backProp!!
        val fkColumn = backProp.storage as Column
        if (fkColumn.onDelete == OnDeleteAction.NONE) {
            throw ExecutionException(
                "Cannot delete the entity '${ctx.owner.entity}', " +
                    "the 'onDelete' of parent property '${backProp}' is 'NONE' " +
                    "but there are some child objects whose type is '${childType.kotlinType.qualifiedName}': " +
                    ctx.detachedTargets.toLimitString { it.entity.toString() }
            )
        }
        if (fkColumn.onDelete == OnDeleteAction.SET_NULL) {
            val (sql, variables) = R2dbcSqlBuilder(sqlClient)
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
            sqlClient.r2dbcExecutor.execute(con, sql, variables) {
                if (rowsUpdated.awaitSingle() != ctx.detachedTargets.size) {
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

    private suspend fun handleMiddleTable(
        contexts: List<MutationContext>,
        prop: EntityProp?,
        backProp: EntityProp?,
        tableName: String,
        joinColumnName: String,
        targetJoinColumnName: String
    ) {
        val (sql, variables) = R2dbcSqlBuilder(sqlClient)
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
        val childGroupMap = sqlClient.r2dbcExecutor.execute(con, sql, variables) {
            mapRows {
                getObject(R2DBC_BASE_INDEX) to
                    getObject(R2DBC_BASE_INDEX + 1)
            }
        }.groupBy({it.first}) {
            it.second
        }
        if (childGroupMap.isNotEmpty()) {
            for (ctx in contexts) {
                val targetIds = childGroupMap[ctx.entityId] ?: continue
                when {
                    prop !== null ->
                        ctx.deleteAssociationAsync(prop) {
                            detachByTargetIds(targetIds)
                            deleteMiddleTableRows(this, tableName, joinColumnName)
                        }
                    backProp !== null ->
                        ctx.deleteAssociationByBackPropAsync(backProp) {
                            detachByTargetIds(targetIds)
                            deleteMiddleTableRows(this, tableName, joinColumnName)
                        }
                    else -> error("Internal bug: neither prop nor backProp is specified")
                }
            }
        }
    }

    private suspend fun deleteMiddleTableRows(
        ctx: MutationContext.AssociationContext,
        tableName: String,
        joinColumnName: String
    ) {
        val (sql, variables) = R2dbcSqlBuilder(sqlClient)
            .apply {
                sql("delete from ")
                sql(tableName)
                sql(" where ")
                sql(joinColumnName)
                sql(" = ")
                variable(ctx.owner.entityId)
            }
            .build()
        sqlClient.r2dbcExecutor.execute(con, sql, variables) {
            if (rowsUpdated.awaitSingle() != ctx.detachedTargets.size) {
                throw ExecutionException("Concurrent modification error")
            }
        }
        ctx.detachedTargets.forEach {
            it.middleTableDeleted()
        }
    }
}