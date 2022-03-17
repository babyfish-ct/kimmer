package org.babyfish.kimmer.sql.common

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.RootMutationResult
import org.babyfish.kimmer.sql.ast.Executable
import java.util.*
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractMutationTest : AbstractTest() {

    protected fun Executable<Int>.executeAndExpectRowCount(
        dataSource: DataSource? = null,
        connectionFactory: ConnectionFactory? = null,
        testWay: TestWay = TestWay.BOTH,
        block: ExpectDSLWithRowCount.() -> Unit
    ) {
        if (testWay.forJdbc) {
            jdbc(TransactionMode.ROLLBACK, dataSource) {
                clearExecutions()
                val (affectedRowCount, throwable) = try {
                    execute(this) to null
                } catch (ex: Throwable) {
                    0 to ex
                }
                assert(throwable, affectedRowCount, block)
            }
        }

        resetAutoIds()

        if (testWay.forR2dbc) {
            runBlocking {
                r2dbc(TransactionMode.ROLLBACK, connectionFactory) {
                    clearExecutions()
                    val (affectedRowCount, throwable) = try {
                        execute(this) to null
                    } catch (ex: Throwable) {
                        0 to ex
                    }
                    assert(throwable, affectedRowCount, block)
                }
            }
        }
    }

    protected fun Executable<RootMutationResult>.executeAndExpectResult(
        dataSource: DataSource? = null,
        connectionFactory: ConnectionFactory? = null,
        testWay: TestWay = TestWay.BOTH,
        init: (java.sql.Connection.() -> Unit)? = null,
        block: ExpectDSLWithResult.() -> Unit
    ) {
        if (testWay.forJdbc) {
            jdbc(TransactionMode.ROLLBACK, dataSource) {
                if (init !== null) {
                    init()
                }
                clearExecutions()
                val (results, throwable) = try {
                    listOf(execute(this)) to null
                } catch (ex: Throwable) {
                    emptyList<RootMutationResult>() to ex
                }
                assert(throwable, results, block)
            }
        }

        resetAutoIds()

        if (testWay.forR2dbc) {
            if (init !== null) {
                jdbc(dataSource = dataSource) {
                    init()
                }
            }
            runBlocking {
                r2dbc(TransactionMode.ROLLBACK, connectionFactory) {
                    clearExecutions()
                    val (results, throwable) = try {
                        listOf(execute(this)) to null
                    } catch (ex: Throwable) {
                        emptyList<RootMutationResult>() to ex
                    }
                    assert(throwable, results, block)
                }
            }
        }
    }

    protected fun Executable<List<RootMutationResult>>.executeAndExpectResults(
        testWay: TestWay = TestWay.BOTH,
        block: ExpectDSLWithResult.() -> Unit
    ) {
        if (testWay.forJdbc) {
            jdbc(TransactionMode.ROLLBACK) {
                clearExecutions()
                val (results, throwable) = try {
                    execute(this) to null
                } catch (ex: Throwable) {
                    emptyList<RootMutationResult>() to ex
                }
                assert(throwable, results, block)
            }
        }

        resetAutoIds()

        if (testWay.forR2dbc) {
            runBlocking {
                r2dbc(TransactionMode.ROLLBACK) {
                    clearExecutions()
                    val (results, throwable) = try {
                        execute(this) to null
                    } catch (ex: Throwable) {
                        emptyList<RootMutationResult>() to ex
                    }
                    assert(throwable, results, block)
                }
            }
        }
    }

    private fun assert(
        throwable: Throwable?,
        rowCount: Int,
        block: ExpectDSLWithRowCount.() -> Unit
    ) {
        val dsl = ExpectDSLWithRowCount(executions, throwable, rowCount)
        dsl.block()
        dsl.close()
    }

    private fun assert(
        throwable: Throwable?,
        results: List<RootMutationResult>,
        block: ExpectDSLWithResult.() -> Unit
    ) {
        val dsl = ExpectDSLWithResult(executions, throwable, results)
        dsl.block()
        dsl.close()
    }

    protected open class ExpectDSL(
        private val executions: List<Execution>,
        protected val throwable: Throwable?
    ) {
        private var statementCount = 0

        private var throwableChecked = false

        fun statement(
            block: StatementDSL.() -> Unit
        ) {
            val index = statementCount++
            if (index < executions.size) {
                StatementDSL(index, executions[index]).block()
            } else if (throwable !== null) {
                throw throwable
            } else {
                fail("Two many statements, max statement count: ${executions.size}")
            }
        }

        fun throwable(block: ThrowableDSL.() -> Unit) {
            expect(true, "No throwable.") { throwable !== null }
            ThrowableDSL(throwable!!).block()
            throwableChecked = true
        }

        open fun close() {
            expect(statementCount, "Error statement count.") {
                executions.size
            }
            if (throwable !== null) {
                if (!throwableChecked) {
                    throw throwable
                }
            }
        }
    }

    protected class ExpectDSLWithRowCount(
        executions: List<Execution>,
        throwable: Throwable?,
        private val rowCount: Int
    ) : ExpectDSL(executions, throwable) {
        fun rowCount(rowCount: Int) {
            if (throwable === null) {
                expect(rowCount, "bad row count") { this.rowCount }
            }
        }
    }

    protected class ExpectDSLWithResult(
        executions: List<Execution>,
        throwable: Throwable?,
        private val results: List<RootMutationResult>
    ): ExpectDSL(executions, throwable) {

        private var resultCount = 0

        fun result(block: () -> String) {
            val index = resultCount++
            if (index < results.size) {
                val result = results[index]
                expect(
                    block().replace("\r", "").replace("\n", ""),
                    "result[$index]"
                ) {
                    result.toString()
                }
            } else if (throwable !== null) {
                throw throwable
            } else {
                fail("Two many results: max result count: ${results.size}")
            }
        }

        override fun close() {
            super.close()
            expect(resultCount, "Error mutation result count") {
                results.size
            }
        }
    }

    protected class StatementDSL(
        private val index: Int,
        private val execution: Execution
    ) {

        fun sql(value: String) {
            expect(
                value.replace("\r", "").replace("\n", ""),
                "statements[$index].sql") {
                execution.sql
            }
        }

        fun sql(block: () -> String) {
            sql(block())
        }

        fun variables(vararg values: Any) {
            expect(values.size, "statements[$index].variables.size.") {
                execution.variables.size
            }
            for (i in values.indices) {
                val exp = values[i]
                val act = execution.variables[i]
                if (exp is ByteArray) {
                    expect(true) {
                        exp.contentEquals(act as ByteArray)
                    }
                } else {
                    expect(exp, "statements[$index].variables[$i].") {
                        act
                    }
                }
            }
        }

        fun variables(block: List<Any>.() -> Unit) {
            execution.variables.block()
        }
    }

    protected class ThrowableDSL(
        private val throwable: Throwable
    ) {
        fun type(type: KClass<out Throwable>) {
            expect(type) { throwable::class }
        }

        fun message(block: () -> String) {
            expect(block().replace("\r", "").replace("\n", "")) {
                throwable.message
            }
        }

        fun detail(block: Throwable.() -> Unit) {
            throwable.block()
        }
    }
}