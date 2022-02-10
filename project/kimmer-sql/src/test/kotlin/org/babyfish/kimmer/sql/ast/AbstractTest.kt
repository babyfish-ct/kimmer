package org.babyfish.kimmer.sql.ast

import io.r2dbc.spi.Connection
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.sql.ast.model.Author
import org.babyfish.kimmer.sql.ast.model.Book
import org.babyfish.kimmer.sql.ast.model.BookStore
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.spi.createSqlClient
import java.lang.UnsupportedOperationException
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.test.expect

abstract class AbstractTest {

    private var _sql: String? = null

    private var _variables: List<Any?>? = null

    private val con = Proxy.newProxyInstance(
        Connection::class.java.classLoader,
        arrayOf(Connection::class.java)
    ) { proxy, method, args ->
        if (method.name == "toString") {
            "Fake connection"
        } else {
            throw UnsupportedOperationException()
        }
    } as Connection

    private val sqlClient = createSqlClient(
        r2dbcExecutor = { sql, variables ->
            _sql = sql
            _variables = variables
            emptyList<Any>()
        }
    ) {

        id(BookStore::id)
        inverseAssociation(BookStore::books, Book::store)

        id(Book::id)
        prop(Book::store, Column("STORE_ID"))
        prop(
            Book::authors,
            MiddleTable("BOOK_AUTHOR_MAPPING", "BOOK_ID", "AUTHOR_ID")
        )

        id(Author::id)
        inverseAssociation(Author::books, Book::authors)
    }

    protected fun <T: Immutable, R> testQuery(
        type: KClass<T>,
        sql: String,
        vararg variables: Any?,
        block: SqlQuery<T>.() -> TypedSqlQuery<T, R>
    ) {
        runBlocking {
            sqlClient.createQuery(type, block).execute(con)
        }
        expect(sql) { _sql }
        expect(variables.toList()) { _variables }
    }

    companion object {

        @JvmStatic
        protected fun String.trimMarginToOneLine() =
            trimMargin()
                .replace("\r", "")
                .replace("\n", "")
    }
}