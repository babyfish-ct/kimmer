package org.babyfish.kimmer.sql.common

import org.babyfish.kimmer.sql.RootMutationResult
import org.babyfish.kimmer.sql.ast.Executable
import kotlin.reflect.KClass
import kotlin.test.BeforeTest
import kotlin.test.expect

abstract class AbstractMutationTest : AbstractTest() {

    @BeforeTest
    fun restoreDatabase() {
        initDatabase()
    }

    protected fun Executable<RootMutationResult>.executeAndExpect(
        block: ExpectDSL.() -> Unit
    ) {
        jdbc {
            clearExecutions()
            val (results, throwable) = try {
                listOf(execute(this)) to null
            } catch (ex: Throwable) {
                emptyList<RootMutationResult>() to ex
            }
            assert(results, throwable, block)
        }
    }

    protected fun Executable<List<RootMutationResult>>.multipleExecuteAndExpect(
        block: ExpectDSL.() -> Unit
    ) {
        jdbc {
            clearExecutions()
            val (results, throwable) = try {
                execute(this) to null
            } catch (ex: Throwable) {
                emptyList<RootMutationResult>() to ex
            }
            assert(results, throwable, block)
        }
    }

    private fun assert(
        results: List<RootMutationResult>,
        throwable: Throwable?,
        block: ExpectDSL.() -> Unit
    ) {
        val dsl = ExpectDSL(executions, results, throwable)
        dsl.block()
        dsl.close()
    }

    protected class ExpectDSL(
        private val executions: List<Execution>,
        private val results: List<RootMutationResult>,
        private val throwable: Throwable?
    ) {
        private var statementCount = 0

        private var resultCount = 0

        private var throwableChecked = false

        fun statement(
            block: StatementDSL.() -> Unit
        ) {
            StatementDSL(statementCount, executions[statementCount++]).block()
        }

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

        fun throwable(type: KClass<out Throwable>, block: () -> String) {
            expect(true) { throwable !== null }
            expect(type) { throwable!!::class }
            expect(block().replace("\r", "").replace("\n", "")) {
                throwable!!.message
            }
            throwableChecked = true
        }

        fun close() {
            expect(statementCount, "Error statement count") {
                executions.size
            }
            expect(resultCount, "Error mutation result count") {
                results.size
            }
            if (throwable !== null) {
                expect(true, "Error throwable count") {
                    throwableChecked
                }
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