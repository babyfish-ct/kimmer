package org.babyfish.kimmer.sql.runtime

import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.produceAsync
import org.babyfish.kimmer.sql.*
import org.babyfish.kimmer.sql.ast.R2dbcSqlBuilder
import org.babyfish.kimmer.sql.ast.eq
import org.babyfish.kimmer.sql.meta.config.*
import io.r2dbc.spi.Connection
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class R2dbcSaver(
    private val sqlClient: SqlClient,
    private val con: Connection
) {
    suspend fun save(entity: Entity<*>, mutationOptions: MutationOptions): MutationContext {
        val ctx = MutationContext(entity, mutationOptions)
        guaranteeFkParents(ctx)
        merge(ctx)
        mergeAssociations(ctx)
        return ctx
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun guaranteeFkParents(ctx: MutationContext) {
        for (prop in ctx.mutationOptions.entityType.props.values) {
            if (prop.isReference && prop.storage is Column) {
                ctx.saveAssociationAsync(prop) {
                    merge(targets[0])
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun merge(ctx: MutationContext) {

        val entity = ctx.entity
        val saveOptions = ctx.mutationOptions

        if (!saveOptions.updatable) {
            insert(ctx)
        } else {
            val type = saveOptions.entityType.kotlinType as KClass<Entity<FakeId>>
            val idProp = saveOptions.entityType.idProp.immutableProp
            if (Immutable.isLoaded(entity, idProp)) {
                val id = Immutable.get(entity, idProp)!!
                if (!saveOptions.insertable) {
                    update(ctx, false, null)
                } else {
                    val existing = sqlClient.createQuery(type) {
                        where { table.get(idProp.kotlinProp as KProperty1<Entity<FakeId>, Any>) eq id }
                        select(table)
                    }.execute(con).firstOrNull()
                    if (existing !== null) {
                        update(ctx, false, existing)
                    } else {
                        insert(ctx)
                    }
                }
            } else {
                val keyProps = saveOptions.keyProps
                    ?: throw ExecutionException(
                        "Cannot save the entity $entity, " +
                            "its id is not loaded, " +
                            "and the keyProps of saveOptions is not specified"
                    )
                val existing = sqlClient.createQuery(type) {
                    for (keyProp in keyProps) {
                        if (!Immutable.isLoaded(entity, keyProp.immutableProp)) {
                            throw ExecutionException(
                                "Cannot save the entity $entity, " +
                                    "its id is not loaded and keyProps of saveOptions contains '$keyProp', " +
                                    "but that key is not loaded"
                            )
                        }
                        val key = Immutable.get(entity, keyProp.immutableProp)
                        if (key === null) {
                            throw ExecutionException(
                                "Cannot save the entity $entity, " +
                                    "its id is not loaded and keyProps of saveOptions contains '$keyProp', " +
                                    "but that key is null"
                            )
                        }
                        where { table.get(keyProp.kotlinProp as KProperty1<Entity<FakeId>, Any>) eq key }
                    }
                    select(table)
                }.execute(con).also { rows ->
                    if (rows.size > 1) {
                        throw ExecutionException(
                            "Cannot save the entity $entity, " +
                                "its id is not loaded and more than one rows match the keyProps [" +
                                saveOptions.keyProps.joinToString { it.name } +
                                "] of saveOptions: " +
                                rows
                        )
                    }
                }.firstOrNull()
                if (existing !== null) {
                    ctx.entity = produceAsync(type, entity as Entity<FakeId>) {
                        Draft.set(this, idProp, existing.id)
                    }
                    update(ctx, true, existing)
                } else if (saveOptions.insertable) {
                    insert(ctx)
                } else {
                    throw ExecutionException(
                        "Cannot insert the entity $entity because the current saveOptions is not insertable"
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun insert(ctx: MutationContext) {
        val entity = ctx.entity
        val saveOptions = ctx.mutationOptions
        val entityType = saveOptions.entityType
        val idProp = entityType.idProp
        val oldId =
            if (Immutable.isLoaded(entity, idProp.immutableProp)) {
                Immutable.get(entity, idProp.immutableProp)
            } else {
                null
            }
        val idGenerator = entityType.idGenerator
        val insertId = oldId ?: when (idGenerator) {
            is UserIdGenerator<*> ->
                idGenerator.get()
            is UUIDIdGenerator ->
                UUID.randomUUID()
            is SequenceIdGenerator ->
                sqlClient.r2dbcExecutor.execute(con, sqlClient.dialect.idFromSequenceSql(idGenerator.sequenceName), emptyList()) {
                    mapRows { getObject(R2DBC_BASE_INDEX) }.first()
                }
            is IdentityIdGenerator ->
                null
            else ->
                throw ExecutionException(
                    "Cannot insert the entity $entity, " +
                        "its id is not loaded, so the id generator of '$idProp' must be specified"
                )
        }?.let {
            convert(it, idProp.returnType)
                ?: throw ExecutionException("Generated id '$it' does not match the type of $idProp")
        }
        val version = entityType.versionProp
            ?.takeIf { Immutable.isLoaded(entity, it.immutableProp) }
            ?.let { Immutable.get(entity, it.immutableProp) }

        val insertProps = entityType.props.values.filter {
            it.storage is Column &&
                (!it.isId || insertId !== null) &&
                (it.isId || it.isVersion || Immutable.isLoaded(entity, it.immutableProp))
        }
        if (insertProps.isEmpty()) {
            throw ExecutionException("Cannot insert the entity '${entity}' with zero columns")
        }

        val (sql, variables) = R2dbcSqlBuilder(sqlClient)
            .apply {
                sql("insert into ")
                sql(entityType.tableName)
                sql("(")
                sql(insertProps.joinToString { (it.storage as Column).name })
                entityType.props.values.filter { !it.isId }
                sql(")")
                if (insertId !== null && idGenerator is IdentityIdGenerator) {
                    sqlClient.dialect.overrideIdentityIdSql?.let {
                        sql(" ")
                        sql(it)
                    }
                }
                sql(" values(")
                insertProps.mapIndexed { index, prop ->
                    if (index != 0) {
                        sql(", ")
                    }
                    val value = when {
                        prop.isId -> insertId
                        prop.isVersion -> version ?: 0
                        else -> Immutable
                            .get(entity, prop.immutableProp)
                            ?.let {
                                if (prop.isReference) {
                                    (it as Entity<*>).id
                                } else {
                                    it
                                }
                            }
                    }
                    if (value !== null) {
                        variable(value)
                    } else if (prop.isReference) {
                        nullVariable(prop.targetType!!.idProp.returnType)
                    } else {
                        nullVariable(prop.returnType)
                    }
                }
                sql(")")
            }.build()

        sqlClient.r2dbcExecutor.execute(con, sql, variables) {
            rowsUpdated.awaitSingle()
        }
        if (oldId === null) {
            val newId = insertId
                ?: sqlClient.r2dbcExecutor.execute(con, sqlClient.dialect.lastIdentitySql, emptyList()) {
                    mapRows {
                        val id = getObject(R2DBC_BASE_INDEX)
                        convert(id, idProp.returnType)
                            ?: throw ExecutionException("Last id '$id' does not match the type of $idProp")
                    }.first()
                }
            ctx.entity = produceAsync(entityType.kotlinType as KClass<Entity<*>>, entity) {
                Draft.set(this, idProp.immutableProp, newId)
            }
        }
        ctx.type = MutationType.INSERT
    }

    private suspend fun update(
        ctx: MutationContext,
        excludeKeyProps: Boolean,
        oldEntity: Entity<*>?
    ) {
        val entity = ctx.entity
        val saveOptions = ctx.mutationOptions
        val entityType = saveOptions.entityType
        val updatedProps =
            entityType.props.values.filter {
                !it.isId &&
                    it.storage is Column &&
                    Immutable.isLoaded(entity, it.immutableProp) &&
                    (!excludeKeyProps || ctx.mutationOptions.keyProps?.contains(it) != true) &&
                    (oldEntity === null || !propEqual(it, entity, oldEntity))
            }
        if (updatedProps.isEmpty()) {
            return
        }
        val id = Immutable.get(entity, entityType.idProp.immutableProp)!!
        val version = entityType
            .versionProp
            ?.takeIf { Immutable.isLoaded(entity, it.immutableProp) }
            ?.let { Immutable.get(entity, it.immutableProp) } as Int?
        val (sql, variables) = R2dbcSqlBuilder(sqlClient)
            .apply {
                sql("update ")
                sql(entityType.tableName)
                sql(" set ")
                updatedProps.forEachIndexed { index, prop ->
                    if (index != 0) {
                        sql(", ")
                    }
                    sql((prop.storage as Column).name)
                    sql(" = ")
                    if (prop.isVersion) {
                        sql((prop.storage as Column).name)
                        sql(" + 1")
                    } else {
                        val value = Immutable
                            .get(entity, prop.immutableProp)
                            ?.let {
                                if (prop.isReference) {
                                    (it as Entity<*>).id
                                } else {
                                    it
                                }
                            }
                        if (value !== null) {
                            variable(value)
                        } else if (prop.isReference) {
                            nullVariable(prop.targetType!!.idProp.returnType)
                        } else {
                            nullVariable(prop.returnType)
                        }
                    }
                }
                sql(" where ")
                sql((entityType.idProp.storage as Column).name)
                sql(" = ")
                variable(id)
                if (version !== null) {
                    sql(" and ")
                    sql((entityType.versionProp!!.storage as Column).name)
                    sql(" = ")
                    variable(version)
                }
            }
            .build()
        val affectedRowCount = sqlClient.r2dbcExecutor.execute(con, sql, variables) {
            rowsUpdated.awaitSingle()
        }
        if (affectedRowCount == 0) {
            if (version !== null) {
                throw OptimisticLockException(entity)
            }
            return
        }
        if (version !== null) {
            ctx.entity = produceAsync(entityType.kotlinType as KClass<Entity<*>>, entity) {
                Draft.set(this, entityType.versionProp!!.immutableProp, version + 1)
            }
        }
        ctx.type = MutationType.UPDATE
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun mergeAssociations(
        ctx: MutationContext
    ) {
        val entity = ctx.entity
        val saveOptions = ctx.mutationOptions
        val entityType = saveOptions.entityType

        for (prop in entityType.props.values) {
            if (prop.targetType === null || prop.storage is Column || !Immutable.isLoaded(entity, prop.immutableProp)) {
                continue
            }
            if (prop.isConnection) {
                throw ExecutionException(
                    "Illegal loaded connection property '$prop', kimmer-sql cannot save connection associations"
                )
            }
            val mappedBy = prop.mappedBy
            if (mappedBy !== null && mappedBy.storage is Column) {
                ctx.saveAssociationAsync(prop) {
                    mergeChildTable(this)
                }
            } else {
                val (middleTableName, joinColumName, targetJoinColumnName) = if (mappedBy !== null) {
                    (mappedBy.storage as MiddleTable).let {
                        Triple(it.tableName, it.targetJoinColumnName, it.joinColumnName)
                    }
                } else {
                    (prop.storage as MiddleTable).let {
                        Triple(it.tableName, it.joinColumnName, it.targetJoinColumnName)
                    }
                }
                ctx.saveAssociationAsync(prop) {
                    mergeMiddleTable(
                        this,
                        middleTableName,
                        joinColumName,
                        targetJoinColumnName
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun mergeChildTable(
        ctx: MutationContext.AssociationContext
    ) {
        val ownerType = ctx.ownerType
        val targetType = ctx.targetType
        val fkProp = ctx.backProp!!
        val fkColumn = fkProp.storage as Column

        val noIdTargets =ctx
            .targets
            .filter {
                !Immutable.isLoaded(it.entity, targetType.idProp.immutableProp)
            }
        noIdTargets.forEach {
            it.entity = produceAsync(targetType.kotlinType as KClass<Entity<*>>, it.entity) {
                Draft.set(
                    this,
                    fkProp.immutableProp,
                    produceAsync(ownerType.kotlinType as KClass<Entity<*>>) {
                        Draft.set(this, ownerType.idProp.immutableProp, ctx.owner.entity.id)
                    }
                )
            }
            merge(it)
        }
        val (existingSql, existingVariables) = R2dbcSqlBuilder(sqlClient)
            .apply {
                sql("select ")
                sql((targetType.idProp.storage as Column).name)
                sql(" from ")
                sql(targetType.tableName)
                sql(" where ")
                sql(fkColumn.name)
                sql(" = ")
                variable(ctx.owner.entity.id)
            }.build()
        val existingTargetIds = sqlClient.r2dbcExecutor.execute(con, existingSql, existingVariables) {
            mapRows {
                convert(getObject(R2DBC_BASE_INDEX), targetType.idProp.returnType)
                    ?: ExecutionException(
                        "The expected type of '${targetType.idProp}' is ${targetType.idProp.returnType}, " +
                            "but the value read from database does not match that type"
                    )
            }
        }
        val detachedTargetIds = existingTargetIds - ctx.targets.map { it.entity.id }.toSet()
        ctx.detachByTargetIds(detachedTargetIds)

        if (detachedTargetIds.isNotEmpty()) {
            if (ctx.targetMutationOptions.deletable) {
                R2dbcDeleter(sqlClient, con).delete(ctx.detachedTargets)
            } else {
                if (!fkProp.isNullable) {
                    throw ExecutionException(
                        "The one-to-many prop '${ctx.associationName}' is not nullable " +
                            "and the 'deleteDetachedObject' is not enabled in the save options, " +
                            "but there are some detached child entities: " +
                            ctx.detachedTargets.toLimitString { it.entity.toString() }
                    )
                }
                val (sql, variables) = R2dbcSqlBuilder(sqlClient)
                    .apply {
                        sql("update ")
                        sql(targetType.tableName)
                        sql(" set ")
                        sql(fkColumn.name)
                        sql(" = null where ")
                        sql((targetType.idProp.storage as Column).name)
                        sql(" in (")
                        detachedTargetIds.forEachIndexed { index, detachedTargetId ->
                            if (index != 0) {
                                sql(", ")
                            }
                            variable(detachedTargetId)
                        }
                        sql(")")
                    }
                    .build()
                sqlClient.r2dbcExecutor.execute(con, sql, variables) {
                    if (rowsUpdated.awaitSingle() != ctx.detachedTargets.size) {
                        throw ExecutionException("Concurrent modification error")
                    }
                }
                for (detachedTarget in ctx.detachedTargets) {
                    detachedTarget.type = MutationType.UPDATE
                    detachedTarget.entity = produceAsync(targetType.kotlinType as KClass<Entity<*>>, detachedTarget.entity) {
                        Draft.set(
                            this,
                            fkProp.immutableProp,
                            null
                        )
                    }
                }
            }
        }
        for (target in ctx.targets) {

            if (target.entity.id !in existingTargetIds) {
                target.entity = produceAsync(targetType.kotlinType as KClass<Entity<*>>, target.entity) {
                    Draft.set(
                        this,
                        fkProp.immutableProp,
                        produceAsync(ownerType.kotlinType as KClass<Entity<*>>) {
                            Draft.set(this, ownerType.idProp.immutableProp, ctx.owner.entity.id)
                        }
                    )
                }
                merge(target)
            }
        }
    }

    private suspend fun mergeMiddleTable(
        ctx: MutationContext.AssociationContext,
        middleTableName: String,
        joinColumnName: String,
        targetJoinColumnName: String
    ) {
        ctx.targets.forEach {
            merge(it)
        }
        val targetType = ctx.targetType
        val (existingSql, existingVariables) = R2dbcSqlBuilder(sqlClient)
            .apply {
                sql("select ")
                sql(targetJoinColumnName)
                sql(" from ")
                sql(middleTableName)
                sql(" where ")
                sql(joinColumnName)
                sql(" = ")
                variable(ctx.owner.entity.id)
            }
            .build()
        val existingTargetIds = sqlClient.r2dbcExecutor.execute(con, existingSql, existingVariables) {
            mapRows {
                convert(getObject(R2DBC_BASE_INDEX), targetType.idProp.returnType)
                    ?: ExecutionException(
                        "The expected type of '${targetType.idProp}' is ${targetType.idProp.returnType}, " +
                            "but the value read from database does not match that type"
                    )
            }
        }
        val detachedTargetIds = existingTargetIds - ctx.targets.map { it.entity.id }.toSet()
        ctx.detachByTargetIds(detachedTargetIds)

        if (detachedTargetIds.isNotEmpty()) {
            val (sql, variables) = R2dbcSqlBuilder(sqlClient)
                .apply {
                    sql("delete from ")
                    sql(middleTableName)
                    sql(" where ")
                    sql(joinColumnName)
                    sql(" = ")
                    variable(ctx.owner.entity.id)
                    sql(" and ")
                    sql(targetJoinColumnName)
                    sql(" in(")
                    ctx.detachedTargets.forEachIndexed { index, target ->
                        if (index != 0) {
                            sql(", ")
                        }
                        variable(target.entity.id)
                    }
                    sql(")")
                }
                .build()
            sqlClient.r2dbcExecutor.execute(con, sql, variables) {
                if (rowsUpdated.awaitSingle() != ctx.detachedTargets.size) {
                    throw ExecutionException("Concurrent modification error")
                }
            }
            ctx.detachedTargets.forEach { it.middleTableDeleted() }
        }

        val attachedTargetIds = ctx.targets.map { it.entity.id } - existingTargetIds
        if (attachedTargetIds.isNotEmpty()) {
            val (insertSql, insertVariables) = R2dbcSqlBuilder(sqlClient)
                .apply {
                    sql("insert into ")
                    sql(middleTableName)
                    sql("(")
                    sql(joinColumnName)
                    sql(", ")
                    sql(targetJoinColumnName)
                    sql(") values")
                    attachedTargetIds.forEachIndexed { index, targetId ->
                        if (index != 0) {
                            sql(", ")
                        }
                        sql("(")
                        variable(ctx.owner.entity.id)
                        sql(", ")
                        variable(targetId)
                        sql(")")
                    }
                }
                .build()
            sqlClient.r2dbcExecutor.execute(con, insertSql, insertVariables) {
                if (rowsUpdated.awaitSingle() != attachedTargetIds.size) {
                    throw ExecutionException("Concurrent modification error")
                }
            }
            for (target in ctx.targets) {
                if (target.entity.id in attachedTargetIds) {
                    target.middleTableInserted()
                }
            }
        }
    }
}