package org.babyfish.kimmer.sql.common

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Result
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.model.*
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.config.*
import org.babyfish.kimmer.sql.meta.enumProviderByString
import org.babyfish.kimmer.sql.model.Author
import org.babyfish.kimmer.sql.model.Book
import org.babyfish.kimmer.sql.model.BookStore
import org.babyfish.kimmer.sql.model.Gender
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.spi.createSqlClient
import org.junit.BeforeClass
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.io.InputStreamReader
import java.sql.PreparedStatement
import java.util.*
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractTest {

    private val dynamicDialect = DynamicDialect()

    private val _executions = mutableListOf<Execution>()

    private var autoIdMap = mutableMapOf<KClass<out Entity<*>>, AutoIds>()

    protected val sqlClient: SqlClient =
        sqlClient(OnDeleteAction.NONE)

    protected fun sqlClient(
        bookStoreOnDelete: OnDeleteAction,
        vararg scalarProviders: ScalarProvider<*, *>
    ): SqlClient =
        createSqlClient(
            dynamicDialect,
            jdbcExecutor = JdbcExecutorImpl(),
            r2dbcExecutor = R2dbcExecutorImpl()
        ) {

            entity(
                BookStore::class,
                idGenerator = UserIdGenerator {
                    autoId(BookStore::class) as UUID
                }
            )
            inverseProp(BookStore::books, Book::store)

            entity(
                Book::class,
                idGenerator = UserIdGenerator {
                    autoId(Book::class) as UUID
                }
            )
            prop(Book::store, Column(onDelete = bookStoreOnDelete))

            prop(
                Book::authors,
                MiddleTable(
                    tableName = "BOOK_AUTHOR_MAPPING",
                    joinColumnName = "BOOK_ID",
                    targetJoinColumnName = "AUTHOR_ID"
                )
            )
            inverseProp(Book::chapters, Chapter::book)

            entity(
                Author::class,
                idGenerator = UserIdGenerator {
                    autoId(Author::class) as UUID
                }
            )
            inverseProp(Author::books, Book::authors)
            prop(Author::fullName, Formula.of<Author, UUID, String> {
                concat(firstName, value(" "), lastName)
            })

            entity(
                Chapter::class,
                idGenerator = SequenceIdGenerator("chapter_id_seq")
            )
            prop(Chapter::book)

            entity(
                Announcement::class,
                idGenerator = IdentityIdGenerator
            )
            prop(Announcement::store)

            scalarProvider(
                enumProviderByString(Gender::class) {
                    map(Gender.MALE, "M")
                    map(Gender.FEMALE, "F")
                }
            )
            scalarProviders.forEach {
                scalarProvider(it)
            }
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
            variables: Collection<Any>,
            block: PreparedStatement.() -> R
        ): R {
            var index = 0
            val formattedSql = sql.replace(JDBC_PARAMETER_REGEX) {
                "$${++index}" // replace jdbc '?' to '$1', '$2', ..., '$N'
            }
            _executions += Execution(formattedSql, variables.toList())
            return DefaultJdbcExecutor.execute(con, sql, variables, block)
        }
    }

    private inner class R2dbcExecutorImpl: R2dbcExecutor {

        override suspend fun <R> execute(
            con: io.r2dbc.spi.Connection,
            sql: String,
            variables: Collection<Any>,
            block: suspend Result.() -> R
        ): R {
            var index = 0
            val formattedSql = sql.replace(JDBC_PARAMETER_REGEX) {
                "$${++index}" // replace jdbc '?' to '$1', '$2', ..., '$N'
            }
            _executions += Execution(formattedSql, variables.toList())
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

    enum class TransactionMode {
        NONE,
        ROLLBACK
    }

    protected enum class TestWay(
        val forJdbc: Boolean,
        val forR2dbc: Boolean
    ) {
        JDBC(true, false),
        R2DBC(false, true),
        BOTH(true, true)
    }

    companion object {

        private const val JDBC_URL = "jdbc:h2:~/test_db"

        private const val R2DBC_URL = "r2dbc:h2:mem:///r2dbc_db"

        @JvmStatic
        private val JDBC_PARAMETER_REGEX = Regex("\\?")

        @JvmStatic
        private val dataSource: DataSource

        @JvmStatic
        private val connectionFactory: ConnectionFactory

        @JvmStatic
        protected fun jdbc(
            transactionMode: TransactionMode = TransactionMode.NONE,
            dataSource: DataSource? = null,
            block: java.sql.Connection.() -> Unit
        ) {
            (dataSource ?: this.dataSource).connection.use {
                when (transactionMode) {
                    TransactionMode.NONE -> it.block()
                    TransactionMode.ROLLBACK -> {
                        it.autoCommit = false
                        try {
                            it.block()
                        } finally {
                            it.rollback()
                        }
                    }
                }
            }
        }

        @JvmStatic
        protected suspend fun r2dbc(
            transactionMode: TransactionMode = TransactionMode.NONE,
            connectionFactory: ConnectionFactory? = null,
            block: suspend io.r2dbc.spi.Connection.() -> Unit
        ) {
            val con = (connectionFactory ?: this.connectionFactory).create().awaitSingle()
            try {
                when (transactionMode) {
                    TransactionMode.NONE -> block(con)
                    TransactionMode.ROLLBACK -> {
                        con.beginTransaction().awaitFirstOrNull()
                        try {
                            block(con)
                        } finally {
                            con.rollbackTransaction().awaitFirstOrNull()
                        }
                    }
                }
            } finally {
                con.close()
            }
        }

        @JvmStatic
        @BeforeClass
        fun initDatabase() {
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
        private fun initJdbcDatabase(con: java.sql.Connection) {
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
        private suspend fun initR2dbcDatabase(con: io.r2dbc.spi.Connection) {
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
            dataSource = SimpleDriverDataSource(org.h2.Driver(), JDBC_URL)
            connectionFactory = ConnectionFactories.get(R2DBC_URL)
        }
    }
}