package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType

class TableSysTypes(
    immutableType: KSType,
    resolver: Resolver
): SysTypes(immutableType, resolver) {

    val tableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_PACKAGE.Table")
        ?.asStarProjectedType()
        ?: noKimmerSqlType("Table")

    val joinableTableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_PACKAGE.JoinableTable")
        ?.asStarProjectedType()
        ?: noKimmerSqlType("Table")

    val joinTypeType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_PACKAGE.JoinType")
        ?.asStarProjectedType()
        ?: noKimmerSqlType("JoinType")

    private fun noKimmerSqlType(simpleName: String): Nothing {
        throw GeneratorException(
            "The 'immutable.table' of ksp options is true, " +
                "but the type '$KIMMER_SQL_AST_PACKAGE.$simpleName' cannot be resolved. " +
                "please add the dependency 'org.babyfish.kimmer:kimmer-sql:\${version}'"
        )
    }
}