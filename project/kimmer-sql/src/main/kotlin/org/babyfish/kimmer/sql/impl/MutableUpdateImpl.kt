package org.babyfish.kimmer.sql.impl

import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.query.impl.UseTableVisitor
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.dialect.UpdateJoin
import java.lang.IllegalStateException
import kotlin.reflect.KClass

internal class MutableUpdateImpl<E: Entity<ID>, ID: Comparable<ID>>(
    sqlClient: SqlClientImpl,
    type: KClass<E>
) : AbstractMutableStatement(
    TableAliasAllocator(),
    sqlClient
), MutableUpdate<E, ID>, Executable<Int>, Ast {

    private val assignmentMap = mutableMapOf<Target, Expression<*>>()

    private val predicates = mutableListOf<NonNullExpression<Boolean>>()

    override val table: TableImpl<E, ID> =
        TableImpl(
            this,
            sqlClient.entityTypeMap[type]
                ?: throw IllegalArgumentException("Cannot create update for unmapped type '${type.qualifiedName}'")
        )

    override fun <X> set(path: NonNullPropExpression<X>, value: NonNullExpression<X>) {
        setImpl(path, value)
    }

    override fun <X> set(path: PropExpression<X>, value: Expression<X>) {
        setImpl(path, value)
    }

    override fun <X: Any> set(path: NonNullPropExpression<X>, value: X) {
        setImpl(path, value(value))
    }

    override fun <X: Any> set(path: PropExpression<X>, value: X?) {
        if (value !== null) {
            setImpl(path, value(value))
        } else if (path.isSelectable){
            setImpl(path, nullValue(path.selectedType.kotlin))
        } else {
            throw IllegalArgumentException("The assigned prop expression must be selectable")
        }
    }

    override fun where(vararg predicates: NonNullExpression<Boolean>?) {
        validateMutable()
        predicates.forEach {
            if (it !== null) {
                this.predicates += it
            }
        }
    }

    override fun where(block: () -> NonNullExpression<Boolean>?) {
        validateMutable()
        val predicate = block()
        if (predicate !== null) {
            predicates += predicate
        }
    }

    override fun execute(con: java.sql.Connection): Int {
        val builder = JdbcSqlBuilder(sqlClient)
        renderTo(builder)
        val (sql, variables) = builder.build()
        return sqlClient.jdbcExecutor.execute(con, sql, variables) {
            executeUpdate()
        }
    }

    override suspend fun execute(con: io.r2dbc.spi.Connection): Int {
        val builder = R2dbcSqlBuilder(sqlClient)
        renderTo(builder)
        val (sql, variables) = builder.build()
        return sqlClient.r2dbcExecutor.execute(con, sql, variables) {
            rowsUpdated.awaitSingle()
        }
    }

    override fun accept(visitor: AstVisitor) {
        assignmentMap.forEach {
            it.key.expr.accept(visitor)
            it.value.accept(visitor)
        }
        predicates.forEach {
            it.accept(visitor)
        }
    }

    private fun setImpl(left: PropExpression<*>, right: Expression<*>) {
        validateMutable()
        val target = targetOf(left)
        if (target.prop.storage !is Column) {
            throw IllegalArgumentException("The assigned prop expression must be mapped as column")
        }
        val joinedTableUpdatable = sqlClient.dialect.updateJoin?.joinedTableUpdatable ?: false
        if (!joinedTableUpdatable && target.table !== table) {
            throw IllegalArgumentException(
                "The current dialect '${sqlClient.dialect::class.qualifiedName}' indicates that " +
                    "only the columns of current table can be updated"
            )
        }
        assignmentMap.put(target, right)?.let {
            throw IllegalStateException("Cannot update same column twice")
        }
    }

    private fun renderTo(builder: SqlBuilder) {
        val sqlBuilder = builder as AbstractSqlBuilder
        this.accept(VisitorImpl(sqlBuilder, sqlClient.dialect))
        sqlBuilder.apply {
            sql("update ")
            sql(table.entityType.tableName)
            sql(" ")
            sql(table.alias)
            if (sqlClient.dialect.updateJoin?.from == UpdateJoin.From.UNNECESSARY) {
                table.childTableMap.values.forEach {
                    it.renderTo(this)
                }
            }
            sql(" set ")
            renderAssignments()
            renderTables()
            renderDeeperJoins()
            renderPredicates()
        }
    }

    private fun AbstractSqlBuilder.renderAssignments() {
        val withTargetPrefix =
            sqlClient.dialect.updateJoin?.joinedTableUpdatable == true &&
                table.childTableMap.values.any { isTableUsed(it) }
        var sp: String? = null
        for (entry in assignmentMap.entries) {
            if (sp !== null) {
                sql(sp)
            } else {
                sp = ", "
            }
            renderTarget(entry.key, withTargetPrefix)
            sql(" = ")
            entry.value.renderTo(this)
        }
    }

    private fun SqlBuilder.renderTarget(target: Target, withPrefix: Boolean) {
        if (withPrefix) {
            sql(target.table.alias)
            sql(".")
        }
        sql((target.prop.storage as Column).name)
    }

    private fun AbstractSqlBuilder.renderTables() {
        if (table.childTableMap.values.any { isTableUsed(it) }) {
            when (sqlClient.dialect.updateJoin!!.from) {
                UpdateJoin.From.AS_ROOT ->
                    table.renderTo(this)
                UpdateJoin.From.AS_JOIN -> {
                    sql(" from ")
                    table.childTableMap.values.forEachIndexed { index, child ->
                        if (index != 0) {
                            sql(", ")
                        }
                        child.renderJoinAsFrom(this, TableImpl.RenderMode.FROM_ONLY)
                    }
                }
            }
        }
    }

    private fun AbstractSqlBuilder.renderDeeperJoins() {
        if (sqlClient.dialect.updateJoin?.from == UpdateJoin.From.AS_JOIN &&
            table.childTableMap.values.any { isTableUsed(it) }
        ) {
            table.childTableMap.values.forEach {
                it.renderJoinAsFrom(this, TableImpl.RenderMode.DEEPER_JOIN_ONLY)
            }
        }
    }

    private fun AbstractSqlBuilder.renderPredicates() {
        var sp = " where "
        if (sqlClient.dialect.updateJoin?.from == UpdateJoin.From.AS_JOIN &&
            table.childTableMap.values.any { isTableUsed(it) }
        ) {
            table.childTableMap.values.forEach {
                sql(sp)
                sp = " and "
                it.renderJoinAsFrom(this, TableImpl.RenderMode.WHERE_ONLY)
            }
        }
        predicates.forEach {
            sql(sp)
            sp = " and "
            it.renderTo(this)
        }
    }

    private fun targetOf(expr: PropExpression<*>): Target {
        val targetTable = expr.table as TableImpl
        return if (targetTable.parent !== null && expr.prop.isId) {
            Target(targetTable.parent, targetTable.joinProp!!, expr)
        } else {
            Target(targetTable, expr.prop, expr)
        }
    }

    private class Target(
        val table: TableImpl<*, *>,
        val prop: EntityProp,
        val expr: PropExpression<*>
    ) {
        override fun hashCode(): Int =
            table.hashCode() * 31 + prop.hashCode()

        override fun equals(other: Any?): Boolean =
            if (other is Target) {
                table === other.table && prop === other.prop
            } else {
                false
            }
    }

    class VisitorImpl(
        builder: AbstractSqlBuilder,
        private val dialect: Dialect
    ) : UseTableVisitor(builder) {

        override fun visitTableReference(table: Table<*, *>, entityProp: EntityProp?) {
            super.visitTableReference(table, entityProp)
            validateTable(table as TableImpl<*, *>)
        }

        private fun validateTable(table: TableImpl<*, *>) {
            if (sqlBuilder.isTableUsed(table)) {
                if (table.parent !== null && (dialect.updateJoin === null)) {
                    throw ExecutionException(
                        "Table joins for update statement is forbidden by the current dialect, " +
                            "but there is a join '$table'."
                    )
                }
                if (table.parent !== null &&
                    table.parent.parent === null &&
                    table.isOuterJoin &&
                    dialect.updateJoin?.from == UpdateJoin.From.AS_JOIN) {
                    throw ExecutionException(
                        "The first level table joins cannot be outer join " +
                            "because current dialect '${dialect::class.qualifiedName}' " +
                            "indicates that the first level table joins in update statement " +
                            "must be rendered as 'from' clause, " +
                            "but there is a first level table join whose join type is outer: '$table'."
                    )
                }
            }
            table.parent?.let {
                validateTable(it)
            }
        }
    }
}