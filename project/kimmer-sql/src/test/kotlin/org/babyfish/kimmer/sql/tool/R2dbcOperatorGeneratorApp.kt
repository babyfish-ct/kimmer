package org.babyfish.kimmer.sql.tool

import java.io.File

fun main(args: Array<String>) {

    val baseDir = args
        .takeIf { it.isNotEmpty() }
        ?.first()
        ?.let {
            if (it.endsWith("/")) it.substring(0, it.length - 1) else it
        }
        ?: throw IllegalArgumentException("No arguments")
    val runtimeDir = File("$baseDir/project/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/runtime")
    if (!runtimeDir.isDirectory) {
        throw IllegalArgumentException("Illegal runtime source code dir: $runtimeDir")
    }
    translateToR2dbc(runtimeDir, "Saver")
    translateToR2dbc(runtimeDir, "Deleter")
}

private fun translateToR2dbc(runtimeDir: File, suffix: String) {
    val jdbcFile = File(runtimeDir, "Jdbc$suffix.kt")
    val r2dbcFile = File(runtimeDir, "R2dbc$suffix.kt")
    val jdbcSourceCode = jdbcFile.readText()
    val r2dbcSourceCode = jdbcSourceCode
        .replace(
            "package org.babyfish.kimmer.sql.runtime\n",
            "package org.babyfish.kimmer.sql.runtime\n\nimport kotlinx.coroutines.reactive.awaitSingle"
        )
        .replace("Jdbc", "R2dbc")
        .replace("jdbc", "r2dbc")
        .replace("JDBC", "R2DBC")
        .replace("fun ", "suspend fun ")
        .replace("java.sql.Connection", "io.r2dbc.spi.Connection")
        .replace("executeUpdate()", "rowsUpdated.awaitSingle()")
        .replace("saveAssociation", "saveAssociationAsync")
        .replace("deleteAssociation(", "deleteAssociationAsync(")
        .replace("deleteAssociationByBackProp(", "deleteAssociationByBackPropAsync(")
        .replace("produce", "produceAsync")
    println(r2dbcSourceCode)
    r2dbcFile.writeText(r2dbcSourceCode)
}
