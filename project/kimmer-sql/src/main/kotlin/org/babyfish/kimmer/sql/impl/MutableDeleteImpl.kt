package org.babyfish.kimmer.sql.impl

import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.JdbcSqlBuilder
import org.babyfish.kimmer.sql.ast.R2dbcSqlBuilder
import org.babyfish.kimmer.sql.ast.query.impl.UseTableVisitor
import org.babyfish.kimmer.sql.ast.table.impl.TableAliasAllocator
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import kotlin.reflect.KClass

internal class MutableDeleteImpl<E: Entity<ID>, ID: Comparable<ID>>(
    sqlClient: SqlClientImpl,
    type: KClass<E>
) : AbstractMutableStatement(
    TableAliasAllocator(),
    sqlClient
), MutableDelete<E, ID>, Executable<Int>, Ast {

    private val predicates = mutableListOf<NonNullExpression<Boolean>>()

    override val table: TableImpl<E, ID> =
        TableImpl(
            this,
            sqlClient.entityTypeMap[type]
                ?: throw IllegalArgumentException("Cannot create update for unmapped type '${type.qualifiedName}'")
        )

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
        println(sql)
        println(variables)
        return 0
    }

    override suspend fun execute(con: io.r2dbc.spi.Connection): Int {
        val builder = R2dbcSqlBuilder(sqlClient)
        renderTo(builder)
        val (sql, variables) = builder.build()
        println(sql)
        println(variables)
        return 0
    }

    override fun accept(visitor: AstVisitor) {
        predicates.forEach {
            it.accept(visitor)
        }
    }

    private fun renderTo(builder: SqlBuilder) {
        accept(UseTableVisitor(builder as AbstractSqlBuilder))
        builder.apply {
            sql("delete ")
            table.renderTo(builder)
            var sp = " where "
            predicates.forEach {
                sql(sp)
                sp = " and "
                it.renderTo(builder)
            }
        }
    }
}