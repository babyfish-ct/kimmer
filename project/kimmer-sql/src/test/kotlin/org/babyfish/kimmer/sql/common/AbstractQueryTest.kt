package org.babyfish.kimmer.sql.common

import kotlinx.coroutines.runBlocking
import org.babyfish.kimmer.sql.ast.query.TypedRootQuery
import org.junit.BeforeClass
import kotlin.test.expect

abstract class AbstractQueryTest : AbstractTest() {

    private var _rows: List<Any?>? = null

    protected fun <R: Any> TypedRootQuery<R>.executeAndExpect(
        testWay: TestWay = TestWay.BOTH,
        block: QueryTestContext<R>.() -> Unit
    ) {
        clearExecutions()
        if (testWay.forJdbc) {
            jdbc {
                _rows = execute(this)
            }
            QueryTestContext<R>().block()
        }
        if (testWay.forR2dbc) {
            runBlocking {
                r2dbc {
                    _rows = execute(this)
                }
                QueryTestContext<R>().block()
            }
        }
    }

    protected inner class QueryTestContext<R> {

        fun sql(block: () -> String) {
            expect(
                block()
                    .replace("\r", "")
                    .replace("\n", "")
            ) {
                executions.last().sql
            }
        }

        fun variables(vararg values: Any?) {
            expect(values.toList()) {
                executions.last().variables
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun rows(block: List<R>.() -> Unit) {
            (_rows as List<R>).block()
        }
    }
}