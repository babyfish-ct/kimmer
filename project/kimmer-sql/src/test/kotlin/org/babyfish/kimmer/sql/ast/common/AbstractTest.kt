package org.babyfish.kimmer.sql.ast.common

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.ast.model.*
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.runtime.Dialect
import org.babyfish.kimmer.sql.runtime.defaultJdbcExecutor
import org.babyfish.kimmer.sql.runtime.defaultR2dbcExecutor
import org.babyfish.kimmer.sql.spi.createSqlClient
import java.io.InputStreamReader
import java.sql.DriverManager
import java.util.*
import kotlin.test.expect

abstract class AbstractTest {

    private val dynamicDialect = DynamicDialect()

    protected val sqlClient = createSqlClient(
        dynamicDialect,
        jdbcExecutor = {
            var index = 0
            _sql = sql.replace(JDBC_PARAMETER_REGEX) {
                "$${++index}" // replace jdbc '?' to '$1', '$2', ..., '$N'
            }
            _variables = variables
            defaultJdbcExecutor().also {
                _rows = it
            }
        },
        r2dbcExecutor = {
            _sql = sql
            _variables = variables
            defaultR2dbcExecutor().also {
                _rows = it
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
    }

    private var _sql: String? = null

    private var _variables: List<Any?>? = null

    private var _rows: List<Any?>? = null

    protected fun <R: Any> TypedRootQuery<*, *, R>.executeAndExpect(
        testWay: TestWay = TestWay.BOTH,
        block: QueryTestContext<R>.() -> Unit
    ) {
        if (testWay.forJdbc) {
            jdbc {
                execute(this)
            }
            QueryTestContext<R>().block()
        }
        if (testWay.forR2dbc) {
            runBlocking {
                r2dbc {
                    execute(this)
                }
                QueryTestContext<R>().block()
            }
        }
    }

    protected enum class TestWay(
        val forJdbc: Boolean,
        val forR2dbc: Boolean
    ) {
        JDBC(true, false),
        R2DBC(false, true),
        BOTH(true, true)
    }

    protected inner class QueryTestContext<R> {

        fun sql(block: () -> String) {
            expect(
                block()
                    .replace("\r", "")
                    .replace("\n", "")
            ) {
                _sql
            }
        }

        fun variables(vararg values: Any?) {
            expect(values.toList()) {
                _variables
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun rows(block: List<R>.() -> Unit) {
            (_rows as List<R>).block()
        }
    }

    protected fun using(dialect: Dialect, block: () -> Unit) {
        dynamicDialect.using(dialect, block)
    }

    companion object {

        private const val JDBC_URL = "jdbc:h2:~/test_db"

        private const val R2DBC_URL = "r2dbc:h2:mem:///r2dbc_db"

        private val JDBC_PARAMETER_REGEX = Regex("\\?")

        @JvmStatic
        private val connectionFactory: ConnectionFactory

        init {

            // Init JDBC
            Class.forName("org.h2.Driver")

            // Init R2DBC
            connectionFactory = ConnectionFactories.get(R2DBC_URL)

            jdbc {
                AbstractTest::class.java.classLoader.getResourceAsStream("TestDatabase.sql").use { stream ->
                    val text = InputStreamReader(stream).readText()
                    text
                        .split(";")
                        .filter { it.isNotBlank() }
                        .forEach {
                            createStatement().executeUpdate(it)
                        }
                }
            }

            runBlocking {
                r2dbc {
                    AbstractTest::class.java.classLoader.getResourceAsStream("TestDatabase.sql").use { stream ->
                        val text = InputStreamReader(stream).readText()
                        text
                            .split(";")
                            .filter { it.isNotBlank() }
                            .forEach {
                                createStatement(it).execute().awaitFirst()
                            }
                    }
                }
            }
        }

        @JvmStatic
        protected fun jdbc(
            block: java.sql.Connection.() -> Unit
        ) {
            DriverManager.getConnection("jdbc:h2:~/test_db").use {
                it.block()
            }
        }

        @JvmStatic
        protected suspend fun r2dbc(
            block: suspend io.r2dbc.spi.Connection.() -> Unit
        ) {
            val con = connectionFactory.create().awaitSingle()
            try {
                block(con)
            } finally {
                con.close()
            }
        }
    }
}