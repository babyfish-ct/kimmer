package org.babyfish.kimmer.sql.common

import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.RootMutationResult
import org.babyfish.kimmer.sql.ast.Executable
import kotlin.reflect.KClass
import kotlin.test.expect

abstract class AbstractMutationTest : AbstractTest() {

    protected fun Executable<Int>.executeAndExpectRowCount(
        rowCount: Int,
        block: ExpectDSL.() -> Unit
    ) {
        jdbc {
            initJdbcDatabase(this)
            clearExecutions()
            val (affectedRowCount, throwable) = try {
                execute(this) to null
            } catch (ex: Throwable) {
                0 to ex
            }
            if (throwable === null) {
                expect(rowCount) { affectedRowCount }
            }
            assert(throwable, emptyList(), block)
        }
        runBlocking {
            resetAutoIds()
            r2dbc {
                initR2dbcDatabase(this)
                clearExecutions()
                val (affectedRowCount, throwable) = try {
                    execute(this) to null
                } catch (ex: Throwable) {
                    0 to ex
                }
                if (throwable === null) {
                    expect(rowCount) { affectedRowCount }
                }
                assert(throwable, emptyList(), block)
            }
        }
    }

    protected fun Executable<RootMutationResult>.executeAndExpectResult(
        block: ExpectDSLWithResult.() -> Unit
    ) {
        jdbc {
            initJdbcDatabase(this)
            clearExecutions()
            val (results, throwable) = try {
                listOf(execute(this)) to null
            } catch (ex: Throwable) {
                emptyList<RootMutationResult>() to ex
            }
            assert(throwable, results, block)
        }
        runBlocking {
            resetAutoIds()
            r2dbc {
                initR2dbcDatabase(this)
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

    protected fun Executable<List<RootMutationResult>>.executeAndExpectResults(
        block: ExpectDSLWithResult.() -> Unit
    ) {
        jdbc {
            initJdbcDatabase(this)
            clearExecutions()
            clearExecutions()
            val (results, throwable) = try {
                execute(this) to null
            } catch (ex: Throwable) {
                emptyList<RootMutationResult>() to ex
            }
            assert(throwable, results, block)
        }
        runBlocking {
            resetAutoIds()
            r2dbc {
                initR2dbcDatabase(this)
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
        private val throwable: Throwable?
    ) {
        private var statementCount = 0

        private var throwableChecked = false

        fun statement(
            block: StatementDSL.() -> Unit
        ) {
            StatementDSL(statementCount, executions[statementCount++]).block()
        }

        fun throwable(type: KClass<out Throwable>, block: () -> String) {
            expect(true) { throwable !== null }
            expect(type) { throwable!!::class }
            expect(block().replace("\r", "").replace("\n", "")) {
                throwable!!.message
            }
            throwableChecked = true
        }

        open fun close() {
            expect(statementCount, "Error statement count") {
                executions.size
            }
            if (throwable !== null) {
                expect(true, "Error throwable count") {
                    throwableChecked
                }
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
            val result = results[index]
            expect(
                block().replace("\r", "").replace("\n", ""),
                "result[$index]"
            ) {
                result.toString()
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
            expect(values.toList(), "statements[$index].variables") {
                execution.variables
            }
        }

        fun variables(block: List<Any>.() -> Unit) {
            execution.variables.block()
        }
    }
}