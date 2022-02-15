package org.babyfish.kimmer.sql.ast.query.impl

import org.babyfish.kimmer.sql.ast.SqlBuilder
import org.babyfish.kimmer.sql.ast.table.impl.TableImpl
import org.babyfish.kimmer.sql.ast.table.impl.TableReferenceVisitor
import org.babyfish.kimmer.sql.meta.EntityProp

internal class UseTableVisitor(
    override val sqlBuilder: SqlBuilder
): TableReferenceVisitor {

    override fun visit(table: TableImpl<*, *>, entityProp: EntityProp?) {
        if (entityProp === null) {
            if (table.entityType.starProps.size > 1) {
                sqlBuilder.useTable(table)
            }
        } else if (!entityProp.isId) {
            sqlBuilder.useTable(table)
        }
    }
}