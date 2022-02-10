package org.babyfish.kimmer.sql.spi

fun databaseIdentifier(name: String): String {
    var prevUpper = true
    return name.toCharArray().joinToString("") {
        val upper = it.isUpperCase()
        val result = if (!prevUpper && upper) {
            "_$it"
        } else {
            it.uppercase()
        }
        prevUpper = upper
        result
    }
}