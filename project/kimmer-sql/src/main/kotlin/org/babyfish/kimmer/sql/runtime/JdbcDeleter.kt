package org.babyfish.kimmer.sql.runtime

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.JdbcSqlBuilder
import org.babyfish.kimmer.sql.ast.query.impl.RootMutableQueryImpl
import org.babyfish.kimmer.sql.impl.SqlClientImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction
import org.babyfish.kimmer.sql.meta.impl.AssociationEntityTypeImpl
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class JdbcDeleter(
    private val sqlClient: SqlClientImpl,
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

    fun delete(
        contexts: Collection<MutationContext>
    ) {
        if (contexts.isNotEmpty()) {
            val mutationOptions = contexts.first().mutationOptions
            if (mutationOptions.entityType is AssociationEntityTypeImpl) {
                deleteAssociationEntities(contexts)
            } else {
                deleteObjectEntities(contexts)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun deleteAssociationEntities(
        contexts: Collection<MutationContext>
    ) {
        val mutationOptions = contexts.first().mutationOptions
        val entityType = mutationOptions.entityType as AssociationEntityTypeImpl
        contexts
            .filter { !it.isEntityInitialized }
            .takeIf { it.isNotEmpty() }
            ?.let { noEntityContexts ->
                val associations = RootMutableQueryImpl<Entity<FakeId>, FakeId>(
                    sqlClient,
                    entityType
                ).run {
                    val sourceExpr = table.joinReference(
                        entityType.sourceProp.immutableProp.kotlinProp as KProperty1<Entity<FakeId>, Entity<FakeId>>
                    )
                    val targetExpr = table.joinReference(
                        entityType.targetProp.immutableProp.kotlinProp as KProperty1<Entity<FakeId>, Entity<FakeId>>
                    )
                    where(
                        tuple {
                            sourceExpr.id as Expression<Any> then
                                targetExpr.id as Expression<Any>
                        } valueIn (noEntityContexts.map { it.entityId as Pair<Any, Any> })
                    )
                    select(table)
                }.execute(con) as List<Association<*, *, *, *>>
                val entityMap = associations.associateBy {
                    it.source.id to it.target.id
                }
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
                val (sql, variables) = JdbcSqlBuilder(sqlClient).apply {
                    sql("delete from ")
                    sql(entityType.tableName)
                    sql(" where (")
                    sql((entityType.sourceProp.storage as Column).name)
                    sql(", ")
                    sql((entityType.targetProp.storage as Column).name)
                    sql(") in (")
                    entityContexts.forEachIndexed { index, ctx ->
                        if (index != 0) {
                            sql(", ")
                        }
                        variable(ctx.entityId)
                    }
                    sql(")")
                }.build()
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
                val (sourceId, targetId) = it.entityId as Pair<Comparable<*>, Comparable<*>>
                it.entity = Association.of(
                    produce(entityType.sourceProp.returnType as KClass<Entity<*>>) {
                        Draft.set(this, Entity<*>::id, sourceId)
                    },
                    produce(entityType.targetProp.returnType as KClass<Entity<*>>) {
                        Draft.set(this, Entity<*>::id, targetId)
                    }
                )
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun deleteObjectEntities(
        contexts: Collection<MutationContext>
    ) {
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
                        ctx.deleteAssociationByBackProp(backProp) {
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

    private fun handleChildTable(
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
        contexts: List<MutationContext>,
        prop: EntityProp?,
        backProp: EntityProp?,
        tableName: String,
        joinColumnName: String,
        targetJoinColumnName: String
    ) {
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
            mapRows {
                getObject(JDBC_BASE_INDEX) to
                    getObject(JDBC_BASE_INDEX + 1)
            }
        }.groupBy({it.first}) {
            it.second
        }
        if (childGroupMap.isNotEmpty()) {
            for (ctx in contexts) {
                val targetIds = childGroupMap[ctx.entityId] ?: continue
                when {
                    prop !== null ->
                        ctx.deleteAssociation(prop) {
                            detachByTargetIds(targetIds)
                            deleteMiddleTableRows(this, tableName, joinColumnName)
                        }
                    backProp !== null ->
                        ctx.deleteAssociationByBackProp(backProp) {
                            detachByTargetIds(targetIds)
                            deleteMiddleTableRows(this, tableName, joinColumnName)
                        }
                    else -> error("Internal bug: neither prop nor backProp is specified")
                }
            }
        }
    }

    private fun deleteMiddleTableRows(
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