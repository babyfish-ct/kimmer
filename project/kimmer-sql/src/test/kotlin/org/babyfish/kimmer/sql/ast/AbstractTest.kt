package org.babyfish.kimmer.sql.ast

import io.r2dbc.spi.Connection
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.model.*
import org.babyfish.kimmer.sql.meta.config.Formula
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

    protected val sqlClient = createSqlClient(
        r2dbcExecutor = { sql, variables ->
            _sql = sql
            _variables = variables
            emptyList<Any>()
        }
    ) {

        inverseProp(BookStore::books, Book::store)
        prop(BookStore::fullName, Formula.of<BookStore, String, String> {
            concat {
                +id
                +"-"
                +name
            }
        })

        prop(Book::store)
        prop(
            Book::authors,
            MiddleTable("BOOK_AUTHOR_MAPPING", "BOOK_ID", "AUTHOR_ID")
        )

        inverseProp(Author::books, Book::authors)
    }

    protected val sql: String?
        get() = _sql

    protected val variables: List<Any?>?
        get() = _variables

    protected fun <E: Entity<ID>, ID: Comparable<ID>> execute(
        query: TypedSqlQuery<E, ID, *>
    ) {
        runBlocking {
            query.execute(con)
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

    companion object {

        @JvmStatic
        protected fun String.toOneLine(): String =
            replace("\r", "")
                .replace("\n", "")
    }
}