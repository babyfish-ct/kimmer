package org.babyfish.kimmer.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType

class TableSysTypes(
    immutableType: KSType,
    resolver: Resolver
): SysTypes(immutableType, resolver) {

    val tableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_TABLE_PACKAGE.Table")
        ?.asStarProjectedType()
        ?: noAstType("Table")

    val nonNullTableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_TABLE_PACKAGE.NonNullTable")
        ?.asStarProjectedType()
        ?: noAstType("Table")

    val joinableTableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_TABLE_PACKAGE.JoinableTable")
        ?.asStarProjectedType()
        ?: noAstType("JoinableTable")

    val nonNullJoinableTableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_TABLE_PACKAGE.NonNullJoinableTable")
        ?.asStarProjectedType()
        ?: noAstType("NonNullJoinableTable")

    val subQueryTableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_TABLE_PACKAGE.SubQueryTable")
        ?.asStarProjectedType()
        ?: noAstType("SubQueryTable")

    val nonNullSubQueryTableType: KSType = resolver
        .getClassDeclarationByName("$KIMMER_SQL_AST_TABLE_PACKAGE.NonNullSubQueryTable")
        ?.asStarProjectedType()
        ?: noAstType("NonNullSubQueryTable")

    private fun noAstType(simpleName: String): Nothing {
        throw GeneratorException(
            "The 'immutable.table' of ksp options is true, " +
                "but the type '$KIMMER_SQL_AST_PACKAGE.$simpleName' cannot be resolved. " +
                "please add the dependency 'org.babyfish.kimmer:kimmer-sql:\${version}'"
        )
    }
}