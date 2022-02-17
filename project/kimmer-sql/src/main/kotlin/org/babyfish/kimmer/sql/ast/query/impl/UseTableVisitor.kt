package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.ast.AbstractSqlBuilder
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.ast.AstVisitor
import org.babyfish.kimmer.sql.ast.table.Table
import org.babyfish.kimmer.sql.meta.EntityProp

internal class UseTableVisitor(
    override val sqlBuilder: AbstractSqlBuilder
): AstVisitor {

    override fun visitTableReference(table: Table<*, *>, entityProp: EntityProp?) {
        val tableImpl = table as TableImpl<*, *>
        if (entityProp === null) {
            if (table.entityType.starProps.size > 1) {
                sqlBuilder.useTable(tableImpl)
            }
        } else if (!entityProp.isId) {
            sqlBuilder.useTable(tableImpl)
        }
    }
}