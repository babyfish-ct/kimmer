package org.babyfish.kimmer.sql.runtime.dialect

import org.babyfish.kimmer.sql.runtime.PaginationContext

class OracleDialect : DefaultDialect() {

    override fun r2dbcParameter(position: Int): String =
        ":$position"

    override fun pagination(ctx: PaginationContext) {
        ctx.apply {
            if (offset == 0) {
                limit()
            } else {
                sql("select * from (")
                limit()
                sql(") limited__ where rn__ > ")
                variable(offset)
            }
        }
    }

    private fun PaginationContext.limit() {
        val rnProjection = if (offset > 0) ", rownum rn__" else ""
        sql("select core__.*$rnProjection from (")
        origin()
        sql(") core__ where rownum <= ")
        variable(offset + limit)
    }

    override fun idFromSequenceSql(sequenceName: String): String =
        "select $sequenceName.nextval from dual"
}