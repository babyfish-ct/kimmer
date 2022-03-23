package org.babyfish.kimmer.runtime

import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.*
import org.babyfish.kimmer.model.*
import org.junit.Test
import kotlin.test.expect

class ConnectionTest {

    @Test
    fun testByDraftProp() {
        val connection = produceConnection(Book::class) {
            edges = listOf(
                produceEdge(Book::class) {
                    node = new(Book::class).by {
                        name = "A"
                    }
                    cursor = "start"
                },
                produceEdge(Book::class) {
                    node = new(Book::class).by {
                        name = "B"
                    }
                    cursor = "end"
                }
            )
            pageInfo = new(Connection.PageInfo::class).by {
                hasPreviousPage = true
                hasNextPage = true
                startCursor = "start"
                endCursor = "end"
            }
            totalCount = 10
        }
        assertConnection(connection)
    }

    @Test
    fun testByDraftFun() {
        val connection = produceConnection(Book::class) {

            edges().add.by {
                node().apply {
                    name = "A"
                }
                cursor = "start"
            }
            edges().add.by {
                node().apply {
                    name = "B"
                }
                cursor = "end"
            }

            pageInfo().apply {
                hasPreviousPage = true
                hasNextPage = true
                startCursor = "start"
                endCursor = "end"
            }
            totalCount = 10
        }
        assertConnection(connection)
    }

    private fun assertConnection(connection: Connection<Book>) {
        val json = connection.toString()
        val clonedConnection = Immutable.fromString(json, Connection::class)

        val newConnection = produceConnection(Book::class, connection) {
            for (edge in edges()) {
                edge.node().name += "*"
                edge.cursor += "*"
            }
            pageInfo().startCursor += "*"
            pageInfo().endCursor += "*"
        }

        expect(false) { connection === clonedConnection }
        expect(true) { connection == clonedConnection }
        expect(true) { connection.toString() == clonedConnection.toString() }

        expect(
            """{
                |"__genericType":"org.babyfish.kimmer.model.Book",
                |"edges":[
                    |{"node":{"name":"A"},"cursor":"start"},
                    |{"node":{"name":"B"},"cursor":"end"}
                |],
                |"pageInfo":{
                    |"endCursor":"end",
                    |"hasNextPage":true,
                    |"hasPreviousPage":true,
                    |"startCursor":"start"
                |},
                |"totalCount":10
            |}""".trimMargin().replace("\r", "").replace("\n", "")
        ) { json }

        expect(
            """{
                |"__genericType":"org.babyfish.kimmer.model.Book",
                |"edges":[
                    |{"node":{"name":"A*"},"cursor":"start*"},
                    |{"node":{"name":"B*"},"cursor":"end*"}
                |],
                |"pageInfo":{
                    |"endCursor":"end*",
                    |"hasNextPage":true,
                    |"hasPreviousPage":true,
                    |"startCursor":"start*"
                |},
                |"totalCount":10
            |}""".trimMargin().replace("\r", "").replace("\n", "")
        ) { newConnection.toString() }
    }

    @Test
    fun testByDraftPropAsync() {
        runBlocking {
            val connection = produceConnectionAsync(Book::class) {
                edges = listOf(
                    produceEdge(Book::class) {
                        node = new(Book::class).by {
                            name = "A"
                        }
                        cursor = "start"
                    },
                    produceEdge(Book::class) {
                        node = new(Book::class).by {
                            name = "B"
                        }
                        cursor = "end"
                    }
                )
                pageInfo = newAsync(Connection.PageInfo::class).by {
                    hasPreviousPage = true
                    hasNextPage = true
                    startCursor = "start"
                    endCursor = "end"
                }
                totalCount = 10
            }
            assertConnectionAsync(connection)
        }
    }

    @Test
    fun testByDraftFunAsync() {
        runBlocking {
            val connection = produceConnectionAsync(Book::class) {

                edges().add.by {
                    node().apply {
                        name = "A"
                    }
                    cursor = "start"
                }
                edges().add.by {
                    node().apply {
                        name = "B"
                    }
                    cursor = "end"
                }

                pageInfo().apply {
                    hasPreviousPage = true
                    hasNextPage = true
                    startCursor = "start"
                    endCursor = "end"
                }
                totalCount = 10
            }
            assertConnectionAsync(connection)
        }
    }

    private suspend fun assertConnectionAsync(connection: Connection<Book>) {
        val json = connection.toString()
        val clonedConnection = Immutable.fromString(json, Connection::class)

        val newConnection = produceConnectionAsync(Book::class, connection) {
            for (edge in edges()) {
                edge.node().name += "*"
                edge.cursor += "*"
            }
            pageInfo().startCursor += "*"
            pageInfo().endCursor += "*"
        }

        expect(false) { connection === clonedConnection }
        expect(true) { connection == clonedConnection }
        expect(true) { connection.toString() == clonedConnection.toString() }

        expect(
            """{
                |"__genericType":"org.babyfish.kimmer.model.Book",
                |"edges":[
                    |{"node":{"name":"A"},"cursor":"start"},
                    |{"node":{"name":"B"},"cursor":"end"}
                |],
                |"pageInfo":{
                    |"endCursor":"end",
                    |"hasNextPage":true,
                    |"hasPreviousPage":true,
                    |"startCursor":"start"
                |},
                |"totalCount":10
            |}""".trimMargin().replace("\r", "").replace("\n", "")
        ) { json }

        expect(
            """{
                |"__genericType":"org.babyfish.kimmer.model.Book",
                |"edges":[
                    |{"node":{"name":"A*"},"cursor":"start*"},
                    |{"node":{"name":"B*"},"cursor":"end*"}
                |],
                |"pageInfo":{
                    |"endCursor":"end*",
                    |"hasNextPage":true,
                    |"hasPreviousPage":true,
                    |"startCursor":"start*"
                |},
                |"totalCount":10
            |}""".trimMargin().replace("\r", "").replace("\n", "")
        ) { newConnection.toString() }
    }
}
