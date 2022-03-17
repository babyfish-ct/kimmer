package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.MutationType
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.impl.RootMutableQueryImpl
import org.babyfish.kimmer.sql.ast.table.NonNullTable
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.runtime.JdbcDeleter
import org.babyfish.kimmer.sql.runtime.MutationContext
import org.babyfish.kimmer.sql.runtime.MutationOptions
import org.babyfish.kimmer.sql.runtime.R2dbcDeleter
import kotlin.reflect.KClass

internal class MutableDeleteImpl<E: Entity<ID>, ID: Comparable<ID>>(
    sqlClient: SqlClientImpl,
    type: KClass<E>
) : AbstractMutableStatement(
    TableAliasAllocator(),
    sqlClient
), MutableDelete<E, ID>, Executable<Int> {

    private val mutableQuery = RootMutableQueryImpl(sqlClient, type)

    override val table: NonNullTable<E, ID>
        get() = mutableQuery.table

    override fun where(vararg predicates: NonNullExpression<Boolean>?) {
        mutableQuery.where(*predicates)
    }

    override fun where(block: () -> NonNullExpression<Boolean>?) {
        mutableQuery.where(block)
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(con: java.sql.Connection): Int {
        val ids = mutableQuery.select(mutableQuery.table.id).distinct().execute(con)
        return if (ids.isEmpty()) {
            0
        } else {
            val entityType = mutableQuery.table.entityType
            val mutationOptions = MutationOptions(
                entityType,
                insertable = false,
                updatable = false,
                deletable = false,
                null,
                mutableMapOf()
            )
            val contexts = ids.map {
                val entity = produce(entityType.kotlinType as KClass<Entity<*>>) {
                    Draft.set(this, entityType.idProp.immutableProp, it)
                }
                MutationContext(entity, mutationOptions)
            }
            JdbcDeleter(sqlClient, con).delete(contexts)
            return contexts.count { it.type == MutationType.DELETE }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(con: io.r2dbc.spi.Connection): Int {
        val ids = mutableQuery.select(mutableQuery.table.id).distinct().execute(con)
        return if (ids.isEmpty()) {
            0
        } else {
            val entityType = mutableQuery.table.entityType
            val mutationOptions = MutationOptions(
                entityType,
                insertable = false,
                updatable = false,
                deletable = false,
                null,
                mutableMapOf()
            )
            val contexts = ids.map {
                val entity = produce(entityType.kotlinType as KClass<Entity<*>>) {
                    Draft.set(this, entityType.idProp.immutableProp, it)
                }
                MutationContext(entity, mutationOptions)
            }
            R2dbcDeleter(sqlClient, con).delete(contexts)
            return contexts.count { it.type == MutationType.DELETE }
        }
    }
}
