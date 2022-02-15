import org.babyfish.kimmer.sql.ast.count
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.sql
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.example.AppContext
import org.babyfish.kimmer.sql.example.model.*

fun main(args: Array<String>) {
    showData(
        input("name filter of current book object"),
        input("name filter of parent store object"),
        input("name filter of child author object"),
    )
}

private fun input(variableDescription: String): String? {
    print("Please input $variableDescription (Optional): ")
    return readLine()?.takeIf { it.isNotBlank() }
}

private fun showData(
    name: String? = null,
    storeName: String? = null,
    authorName: String? = null,
    pageSize: Int = 2
) {

    val query = AppContext.sqlClient.createQuery(Book::class) {

        name?.let {
            where(table.name ilike it)
        }

        storeName?.let {
            where(table.store.name ilike it)
        }

        authorName?.let {
            where(
                table.id valueIn subQuery(Author::class) {
                    where(table.fullName ilike it)
                    select(table.books.id)
                }
            )
        }

        orderBy(table.name)

        select(
            table,
            sql(Int::class, "rank() over(order by %e desc)") {
                expressions(table.price)
            },
            sql(Int::class, "rank() over(partition by %e order by %e desc)") {
                expressions(table.store.id, table.price)
            }
        )
    }

    val countQuery = query.reselect {
        select(table.id.count())
    }.withoutSortingAndPaging()

    val rowCount = AppContext.jdbc {
        countQuery.execute(this)[0].toInt()
    }
    val pageCount = (rowCount + pageSize - 1) / pageSize
    println("-------------------------------------------------")
    println("Total row count: $rowCount, pageCount: $pageCount")
    println("-------------------------------------------------")
    println()

    for (pageNo in 1..pageCount) {
        println("-----------Page no: $pageNo-----------")
        println()
        val rows = AppContext.jdbc {
            query.limit(pageSize, pageSize * (pageNo - 1)).execute(this)
        }
        for ((book, rank, partitionRank) in rows) {
            println("book object: $book")
            println("global price rank: $rank")
            println("price rank in own store: $partitionRank")
            println()
        }
    }
}