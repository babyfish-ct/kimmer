package org.babyfish.kimmer.sql.example

import org.babyfish.kimmer.sql.example.model.*

import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.example.model.Author
import org.babyfish.kimmer.sql.example.model.Book
import org.babyfish.kimmer.sql.example.model.BookStore
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.enumProviderByString
import org.babyfish.kimmer.sql.runtime.DefaultJdbcExecutor
import org.babyfish.kimmer.sql.runtime.JdbcExecutor
import org.babyfish.kimmer.sql.spi.createSqlClient
import java.io.InputStreamReader
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.*

object AppContext {

    val sqlClient = createSqlClient(
        jdbcExecutor = object: JdbcExecutor {
            override fun <R> execute(
                con: Connection,
                sql: String,
                variables: Collection<Any>,
                block: PreparedStatement.() -> R
            ): R {
                // System.err prints red text in intellij, not white
                System.err.println("jdbc sql: $sql")
                System.err.println("jdbc parameters: ${variables.joinToString { "\"$it\"" }}")
                return DefaultJdbcExecutor.execute(con, sql, variables, block)
            }
        }
    ) {

        inverseProp(BookStore::books, Book::store)

        prop(Book::store)
        prop(
            Book::authors,
            MiddleTable(
                tableName = "BOOK_AUTHOR_MAPPING",
                joinColumnName = "BOOK_ID",
                targetJoinColumnName = "AUTHOR_ID"
            )
        )

        inverseProp(Author::books, Book::authors)
        prop(Author::fullName, Formula.of<Author, UUID, String> {
            concat(firstName, value(" "), lastName)
        })

        scalarProvider(
            enumProviderByString(Gender::class) {
                map(Gender.MALE, "M")
                map(Gender.FEMALE, "F")
            }
        )
    }

    fun <R> jdbc(block: Connection.() -> R): R =
        DriverManager.getConnection("jdbc:h2:~/example").use {
            it.block()
        }

    init {

        Class.forName("org.h2.Driver")

        val stream = this::class.java.classLoader.getResourceAsStream("database.sql")
            ?: error("Failed to load 'database.sql'")

        val sql = stream.use {
            InputStreamReader(it).readText()
        }

        jdbc {
            createStatement().executeUpdate(sql)
        }
    }
}