package org.babyfish.kimmer.sql.ast.common

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.SqlQuery
import org.babyfish.kimmer.sql.ast.query.TypedSqlQuery
import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.ast.model.*
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.runtime.defaultR2dbcExecutor
import org.babyfish.kimmer.sql.spi.createSqlClient
import java.io.InputStreamReader
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.expect

abstract class AbstractTest {

    private var _sql: String? = null

    private var _variables: List<Any?>? = null

    private var con: Connection? = null

    protected val sqlClient = createSqlClient(
        r2dbcExecutor = {
            _sql = sql
            _variables = variables
            defaultR2dbcExecutor()
        }
    ) {

        inverseProp(BookStore::books, Book::store)

        prop(Book::store)
        prop(
            Book::authors,
            MiddleTable("BOOK_AUTHOR_MAPPING", "BOOK_ID", "AUTHOR_ID")
        )

        inverseProp(Author::books, Book::authors)
        prop(Author::fullName, Formula.of<Author, UUID, String> {
            concat {
                +firstName
                +" "
                +lastName
            }
        })
    }

    protected val sql: String?
        get() = _sql

    protected val variables: List<Any?>?
        get() = _variables

    protected fun <E: Entity<ID>, ID: Comparable<ID>> execute(
        query: TypedSqlQuery<E, ID, *>
    ) {
        runBlocking {
            query.execute(con ?: error("No connection"))
        }
    }

    protected fun <E: Entity<ID>, ID: Comparable<ID>, R> testQuery(
        type: KClass<E>,
        sql: String,
        vararg variables: Any?,
        block: SqlQuery<E, ID>.() -> TypedSqlQuery<E, ID, R>
    ) {
        execute(sqlClient.createQuery(type, block))
        expect(sql) { _sql }
        expect(variables.toList()) { _variables }
    }

    @BeforeTest
    fun createConnection() {
        con = runBlocking {
            connectionFactory.create().awaitSingle()
        }
    }

    @AfterTest
    fun closeConnection() {
        con?.let {
            con = null
            runBlocking {
                it.close()
            }
        }
    }

    companion object {

        @JvmStatic
        protected fun String.toOneLine(): String =
            replace("\r", "")
                .replace("\n", "")

        private val connectionFactory =
            ConnectionFactories.get("r2dbc:h2:mem:///test_db")

        init {
            runBlocking {
                val con = connectionFactory.create().awaitSingle()
                try {
                    AbstractTest::class.java.classLoader.getResourceAsStream("TestDatabase.sql").use { stream ->
                        val text = InputStreamReader(stream).readText()
                        text
                            .split(";")
                            .filter { it.isNotBlank() }
                            .forEach {
                                con.createStatement(it).execute().awaitFirst()
                            }
                    }
                } finally {
                    con.close()
                }
            }
        }
    }
}