package org.babyfish.kimmer.sql.common

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.meta.config.*
import org.babyfish.kimmer.sql.meta.enumProviderByString
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.BookStore
import org.babyfish.kimmer.sql.model.Gender
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.spi.createSqlClient
import java.io.InputStreamReader
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractTest {

    private val dynamicDialect = DynamicDialect()

    private val _executions = mutableListOf<Execution>()

    private var autoIdMap = mutableMapOf<KClass<out Entity<*>>, AutoIds>()

    protected val sqlClient: SqlClient =
        sqlClient(OnDeleteAction.NONE)

    protected fun sqlClient(bookStoreOnDelete: OnDeleteAction): SqlClient =
        createSqlClient(
            dynamicDialect,
            jdbcExecutor = JdbcExecutorImpl(),
            r2dbcExecutor = R2dbcExecutorImpl()
        ) {

            prop(
                BookStore::id,
                idGenerator = UserIdGenerator {
                    autoId(BookStore::class) as UUID
                }
            )
            inverseProp(BookStore::books, Book::store)

            prop(
                Book::id,
                idGenerator = UserIdGenerator {
                    autoId(Book::class) as UUID
                }
            )
            prop(Book::store, Column("STORE_ID", onDelete = bookStoreOnDelete))

            prop(
                Book::authors,
                MiddleTable(
                    tableName = "BOOK_AUTHOR_MAPPING",
                    joinColumnName = "BOOK_ID",
                    targetJoinColumnName = "AUTHOR_ID"
                )
            )

            prop(
                Author::id,
                idGenerator = UserIdGenerator {
                    autoId(Author::class) as UUID
                }
            )
            inverseProp(Author::books, Book::authors)
            prop(Author::fullName, Formula.of<Author, UUID, String> {
                concat(firstName, value(" "), lastName)
            })

            scalar(
                enumProviderByString(Gender::class) {
                    map(Gender.MALE, "M")
                    map(Gender.FEMALE, "F")
                }
            )
        }

    protected fun using(dialect: Dialect, block: () -> Unit) {
        dynamicDialect.using(dialect, block)
    }

    protected val executions: List<Execution>
        get() = _executions

    protected fun clearExecutions() {
        _executions.clear()
    }

    protected fun autoIds(type: KClass<out Entity<*>>, vararg ids: Any) {
        autoIdMap[type] = AutoIds(ids.toList())
    }

    private fun autoId(type: KClass<out Entity<*>>): Any {
        val autoIds = autoIdMap[type] ?: error("No prepared auto ids for ${type.qualifiedName}")
        return autoIds.get()
    }

    protected fun resetAutoIds() {
        autoIdMap.values.forEach {
            it.reset()
        }
    }

    private inner class JdbcExecutorImpl: JdbcExecutor {

        override fun <R> execute(
            con: java.sql.Connection,
            sql: String,
            variables: List<Any>,
            block: PreparedStatement.() -> R
        ): R {
            var index = 0
            val sql = sql.replace(JDBC_PARAMETER_REGEX) {
                "$${++index}" // replace jdbc '?' to '$1', '$2', ..., '$N'
            }
            _executions += Execution(sql, variables)
            return DefaultJdbcExecutor.execute(con, sql, variables, block)
        }
    }

    private inner class R2dbcExecutorImpl: R2dbcExecutor {

        override suspend fun <R> execute(
            con: io.r2dbc.spi.Connection,
            sql: String,
            variables: List<Any>,
            block: suspend Result.() -> R
        ): R {
            _executions += Execution(sql, variables)
            return DefaultR2dbcExecutor.execute(con, sql, variables, block)
        }
    }

    private class AutoIds(
        val ids: List<Any>
    ) {
        var index = 0

        fun get(): Any = ids[index++]

        fun reset() {
            index = 0
        }
    }

    protected class Execution(
        val sql: String,
        val variables: List<Any>
    )

    companion object {

        private const val JDBC_URL = "jdbc:h2:~/test_db"

        private const val R2DBC_URL = "r2dbc:h2:mem:///r2dbc_db"

        private val JDBC_PARAMETER_REGEX = Regex("\\?")

        @JvmStatic
        private val connectionFactory: ConnectionFactory

        @JvmStatic
        protected fun jdbc(
            block: java.sql.Connection.() -> Unit
        ) {
            DriverManager.getConnection(JDBC_URL).use {
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

        @JvmStatic
        protected fun initDatabase() {
            jdbc {
                initJdbcDatabase(this)
            }
            runBlocking {
                r2dbc {
                    initR2dbcDatabase(this)
                }
            }
        }

        @JvmStatic
        protected fun initJdbcDatabase(con: java.sql.Connection) {
            AbstractTest::class.java.classLoader.getResourceAsStream("TestDatabase.sql").use { stream ->
                val text = InputStreamReader(stream).readText()
                text
                    .split(";")
                    .filter { it.isNotBlank() }
                    .forEach {
                        con.createStatement().executeUpdate(it)
                    }
            }
        }

        @JvmStatic
        protected suspend fun initR2dbcDatabase(con: io.r2dbc.spi.Connection) {
            AbstractTest::class.java.classLoader.getResourceAsStream("TestDatabase.sql").use { stream ->
                val text = InputStreamReader(stream ?: error("Cannot load embedded SQL")).readText()
                text
                    .split(";")
                    .filter { it.isNotBlank() }
                    .forEach {
                        con.createStatement(it).execute().awaitFirst()
                    }
            }
        }

        @JvmStatic
        protected fun throwable(
            type: KClass<out Throwable>,
            throwableMessage: String,
            block: () -> Unit
        ) {
            try {
                block()
                fail("Exception expected")
            } catch (ex: Throwable) {
                expect(type) { ex::class }
                expect(throwableMessage.replace("\r", "").replace("\n", "")) {
                    ex.message
                }
            }
        }

        init {
            // Init JDBC
            Class.forName("org.h2.Driver")

            // Init R2DBC
            connectionFactory = ConnectionFactories.get(R2DBC_URL)
        }
    }
}