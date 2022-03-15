package org.babyfish.kimmer.sql.tool

import java.io.File
import kotlin.io.path.Path


fun main(args: Array<String>) {

    val baseDir = args
        .takeIf { it.isNotEmpty() }
        ?.first()
        ?.let {
            if (it.endsWith("/")) it.substring(0, it.length - 1) else it
        }
        ?: throw IllegalArgumentException("No arguments")
    val runtimeDir = "$baseDir/project/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/runtime"
    println(File(runtimeDir).isDirectory)
}