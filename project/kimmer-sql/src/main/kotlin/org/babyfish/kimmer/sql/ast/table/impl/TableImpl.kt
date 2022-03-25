package org.babyfish.kimmer.sql.ast.table.impl

import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ExecutionException
import org.babyfish.kimmer.sql.MappingException
import org.babyfish.kimmer.sql.ast.*
import org.babyfish.kimmer.sql.ast.ContainsExpression
import org.babyfish.kimmer.sql.ast.PropExpressionImpl
import org.babyfish.kimmer.sql.spi.Renderable
import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.query.MutableSubQuery
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.ast.table.NonNullTable
import org.babyfish.kimmer.sql.meta.EntityProp
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.ast.AstVisitor
import org.babyfish.kimmer.sql.impl.AbstractMutableStatement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal open class TableImpl<E: Entity<ID>, ID: Comparable<ID>>(
    private val statement: AbstractMutableStatement,
    val entityType: EntityType,
    val parent: TableImpl<*, *>? = null,
    val isInverse: Boolean = false,
    val joinProp: EntityProp? = null,
    private var _isOuterJoin: Boolean = false
): NonNullTable<E, ID>, Renderable, Selection<E>, Ast {

    internal val alias: String

    internal val middleTableAlias: String?

    internal val childTableMap = mutableMapOf<String, TableImpl<*, *>>()

    init {
        if ((parent === null) != (joinProp === null)) {
            error("Internal bug: Bad constructor arguments for TableImpl")
        }
        middleTableAlias = joinProp
            ?.storage
            ?.let {
                (it as? MiddleTable)?.let {
                    statement.tableAliasAllocator.allocate()
                }
            }
        alias = statement.tableAliasAllocator.allocate()
    }

    protected open fun <X: Entity<XID>, XID: Comparable<XID>> createChildTable(
        statement: AbstractMutableStatement,
        entityType: EntityType,
        isInverse: Boolean,
        joinProp: EntityProp,
        outerJoin: Boolean
    ): TableImpl<X, XID> =
        TableImpl(
            statement,
            if (isInverse) joinProp.declaringType else joinProp.targetType!!,
            this,
            isInverse,
            joinProp,
            outerJoin
        )

    override val id: NonNullPropExpression<ID>
        get() =
            PropExpressionImpl(this, entityType.idProp)

    override fun <X : Any> get(prop: KProperty1<E, X>): NonNullPropExpression<X> =
        `get?`(prop) as NonNullPropExpression<X>

    override fun <X: Any> `get?`(prop: KProperty1<E, X?>): PropExpression<X> {
        val entityProp = entityType.props[prop.name] ?: error("No property '${prop.name}'")
        return PropExpressionImpl(this, entityProp)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X: Entity<XID>, XID: Comparable<XID>> joinReference(
        prop: KProperty1<E, X?>
    ): NonNullTable<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, false, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X: Entity<XID>, XID: Comparable<XID>> `joinReference?`(
        prop: KProperty1<E, X?>
    ): Table<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, true, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinList(
        prop: KProperty1<E, List<X>>
    ): NonNullTable<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, false, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `joinList?`(
        prop: KProperty1<E, List<X>>
    ): Table<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, true, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> joinConnection(
        prop: KProperty1<E, Connection<X>>
    ): NonNullTable<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, false, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `joinConnection?`(
        prop: KProperty1<E, Connection<X>>
    ): Table<X, XID> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, true, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinReference`(
        prop: KProperty1<X, E?>
    ): NonNullTable<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, false, true)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinReference?`(
        prop: KProperty1<X, E?>
    ): Table<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        return join0(entityProp, true, true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinList`(
        prop: KProperty1<X, List<E>>
    ): NonNullTable<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, false, true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinList?`(
        prop: KProperty1<X, List<E>>
    ): Table<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return join0(entityProp, true, true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinConnection`(
        prop: KProperty1<X, Connection<E>>
    ): NonNullTable<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, false, true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←joinConnection?`(
        prop: KProperty1<X, Connection<E>>
    ): Table<X, XID> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return join0(entityProp, true, true)
    }

    private fun <X: Entity<XID>, XID: Comparable<XID>> join0(
        entityProp: EntityProp,
        outerJoin: Boolean,
        inverse: Boolean
    ): NonNullTable<X, XID> {

        if (entityProp.isTransient) {
            throw ExecutionException("Cannot join to '$entityProp' because it's transient association")
        }
        statement.validateMutable()

        val joinName = if (!inverse) {
            entityProp.name
        } else {
            entityProp?.opposite?.name
                ?: "inverse(${entityProp.kotlinProp})"
        }
        return if (entityProp.mappedBy !== null) {
            join1(joinName, !inverse, entityProp.mappedBy!!, outerJoin)
        } else {
            join1(joinName, inverse, entityProp, outerJoin)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <X: Entity<XID>, XID: Comparable<XID>> join1(
        joinName: String,
        inverse: Boolean,
        joinProp: EntityProp,
        outerJoin: Boolean
    ): NonNullTable<X, XID> {
        val existing = childTableMap[joinName]
        if (existing !== null) {
            if (!outerJoin) {
                existing._isOuterJoin = false
            }
            return existing as NonNullTable<X, XID>
        }
        val newTable =
            this.createChildTable<X, XID>(
                statement,
                if (inverse) joinProp.declaringType else joinProp.targetType!!,
                inverse,
                joinProp,
                outerJoin
            )
        childTableMap[joinName] = newTable
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
                statement.sqlClient.entityTypeMap[it]
            } ?: error("The declaring type of reversed prop is not mapped entity")

    override fun <X: Entity<XID>, XID: Comparable<XID>> listContainsAny(
        prop: KProperty1<E, List<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return containsAny0(entityProp, xIds, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> connectionContainsAny(
        prop: KProperty1<E, Connection<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return containsAny0(entityProp, xIds, false)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←listContainsAny`(
        prop: KProperty1<X, List<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return containsAny0(entityProp, xIds, true)
    }

    override fun <X: Entity<XID>, XID: Comparable<XID>> `←connectionContainsAny`(
        prop: KProperty1<X, Connection<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return containsAny0(entityProp, xIds, true)
    }
    override fun <X : Entity<XID>, XID : Comparable<XID>> listContainsAll(
        prop: KProperty1<E, List<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return containsAll0(entityProp, xIds, false)
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> connectionContainsAll(
        prop: KProperty1<E, Connection<X>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return containsAll0(entityProp, xIds, false)
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←listContainsAll`(
        prop: KProperty1<X, List<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        return containsAll0(entityProp, xIds, true)
    }

    override fun <X : Entity<XID>, XID : Comparable<XID>> `←connectionContainsAll`(
        prop: KProperty1<X, Connection<E>>,
        xIds: Collection<XID>
    ): NonNullExpression<Boolean> {
        val entityProp = reverseType(prop).props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        return containsAll0(entityProp, xIds, true)
    }

    private fun containsAny0(
        prop: EntityProp,
        xIds: Collection<Any>,
        inverse: Boolean
    ): NonNullExpression<Boolean> {
        if (prop.isTransient) {
            throw ExecutionException("Cannot invoke containsAny on '$prop' because it's transient association")
        }
        return if (prop.mappedBy !== null) {
            ContainsExpression(this, prop.mappedBy!!, false, xIds, !inverse)
        } else {
            ContainsExpression(this, prop, false, xIds, inverse)
        }
    }

    private fun containsAll0(
        prop: EntityProp,
        xIds: Collection<Any>,
        inverse: Boolean
    ): NonNullExpression<Boolean> {
        if (prop.isTransient) {
            throw ExecutionException("Cannot invoke containsAll on '$prop' because it's transient association")
        }
        return if (prop.mappedBy !== null) {
            ContainsExpression(this, prop.mappedBy!!, true, xIds, !inverse)
        } else {
            ContainsExpression(this, prop, true, xIds, inverse)
        }
    }

    override fun renderTo(builder: SqlBuilder) {
        builder.renderSelf()
        if (isUsedBy(builder)) {
            for (childTable in childTableMap.values) {
                childTable.renderTo(builder)
            }
        }
    }

    fun renderJoinAsFrom(builder: SqlBuilder, mode: RenderMode) {
        if (parent === null) {
            error("Internal bug: renderJoinAsFrom can only be called base on joined tables")
        }
        if (mode === RenderMode.NORMAL) {
            error("Internal bug: renderJoinAsFrom does not accept render mode ALL")
        }
        if (isUsedBy(builder)) {
            builder.renderSelf(mode)
            if (mode == RenderMode.DEEPER_JOIN_ONLY) {
                for (childTable in childTableMap.values) {
                    childTable.renderTo(builder)
                }
            }
        }
    }

    override fun accept(visitor: AstVisitor) {
        visitor.visitTableReference(this, null)
    }

    private fun SqlBuilder.renderSelf(mode: RenderMode = RenderMode.NORMAL) {
        if (isInverse) {
            renderInverseJoin(mode)
        } else if (joinProp !== null) {
            renderJoin(mode)
        } else {
            sql(" from ")
            sql(entityType.tableName)
            sql(" as ")
            sql(alias)
        }
    }

    private fun SqlBuilder.renderJoin(mode: RenderMode) {

        val parent = parent!!
        val isOuterJoin = _isOuterJoin
        val middleTable = joinProp!!.storage as? MiddleTable

        if (middleTable !== null) {
            joinImpl(
                isOuterJoin,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.joinColumnName,
                mode
            )
            if (isUsedBy(this) && (mode == RenderMode.NORMAL || mode == RenderMode.DEEPER_JOIN_ONLY)) {
                joinImpl(
                    isOuterJoin,
                    middleTableAlias!!,
                    middleTable.targetJoinColumnName,
                    entityType.tableName,
                    alias,
                    (entityType.idProp.storage as Column).name,
                    RenderMode.NORMAL
                )
            }
        } else if (isUsedBy(this)) {
            joinImpl(
                isOuterJoin,
                parent.alias,
                (joinProp.storage as Column).name,
                entityType.tableName,
                alias,
                (parent.entityType.idProp.storage as Column).name,
                mode
            )
        }
    }

    private fun SqlBuilder.renderInverseJoin(mode: RenderMode) {

        val parent = parent!!
        val isOuterJoin = _isOuterJoin
        val middleTable = joinProp!!.storage as? MiddleTable

        if (middleTable !== null) {
            joinImpl(
                isOuterJoin,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.targetJoinColumnName,
                mode
            )
            if (isUsedBy(this) && (mode == RenderMode.NORMAL || mode == RenderMode.DEEPER_JOIN_ONLY)) {
                joinImpl(
                    isOuterJoin,
                    middleTableAlias!!,
                    middleTable.joinColumnName,
                    entityType.tableName,
                    alias,
                    (entityType.idProp.storage as Column).name,
                    RenderMode.NORMAL
                )
            }
        } else { // One-to-many join cannot be optimized by "_used"
            joinImpl(
                isOuterJoin,
                parent.alias,
                (parent.entityType.idProp.storage as Column).name,
                entityType.tableName,
                alias,
                (joinProp.storage as Column).name,
                mode
            )
        }
    }

    private fun SqlBuilder.joinImpl(
        isOuterJoin: Boolean,
        previousAlias: String,
        previousColumnName: String,
        newTableName: String,
        newAlias: String,
        newColumnName: String,
        mode: RenderMode
    ) {
        if (mode !== RenderMode.NORMAL && isOuterJoin) {
            error("Internal bug: outer join cannot be accepted by abnormal render mode")
        }
        when (mode) {
            RenderMode.NORMAL -> {
                val jt = if (isOuterJoin) "left" else "inner"
                sql(" ")
                sql(jt)
                sql(" join ")
                sql(newTableName)
                sql(" as ")
                sql(newAlias)
                sql(" on ")
            }
            RenderMode.FROM_ONLY -> {
                sql(newTableName)
                sql(" as ")
                sql(newAlias)
            }
        }
        if (mode == RenderMode.NORMAL || mode == RenderMode.WHERE_ONLY) {
            sql(previousAlias)
            sql(".")
            sql(previousColumnName)
            sql(" = ")
            sql(newAlias)
            sql(".")
            sql(newColumnName)
        }
    }

    private fun isUsedBy(builder: SqlBuilder): Boolean =
        parent === null || (builder as AbstractSqlBuilder).isTableUsed(this)

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

    @Suppress("UNCHECKED_CAST")
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
            if (storage is Formula<*, *, *>) {
                (this as AbstractSqlBuilder).resolveFormula(prop) {
                    (storage as Formula<E, ID, Any>).get(this@TableImpl).let {
                        if (starMode && it is MutableSubQuery<*, *, *, *>) {
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

    val isOuterJoin: Boolean
        get() = _isOuterJoin

    val destructive: Destructive
        get() {
            if (joinProp === null) {
                return Destructive.NONE
            }
            val prop =
                if (isInverse) {
                    (joinProp.opposite ?: return Destructive.BREAK_REPEATABILITY)
                } else {
                    joinProp
                }
            if (prop.isList || prop.isConnection) {
                return Destructive.BREAK_REPEATABILITY
            }
            if (prop.isNullable && !_isOuterJoin) {
                return Destructive.BREAK_ROW_COUNT
            }
            return Destructive.NONE
        }

    override fun toString(): String =
        when {
            joinProp === null ->
                entityType.kotlinType.simpleName!!
            isInverse ->
                joinProp.opposite?.let {
                    "$parent.${it.name}"
                } ?: "← $parent.${joinProp.name}"
            else ->
                "$parent.${joinProp.name}"
        }.let {
            if (_isOuterJoin) {
                "$it?"
            } else {
                it
            }
        }

    enum class Destructive {
        NONE, // Left join for nullable reference, Left/Inner join for non-null reference
        BREAK_ROW_COUNT, // inner join for nullable-reference
        BREAK_REPEATABILITY // Any join for Collection
    }

    enum class RenderMode {
        NORMAL,
        FROM_ONLY,
        WHERE_ONLY,
        DEEPER_JOIN_ONLY;
    }
}