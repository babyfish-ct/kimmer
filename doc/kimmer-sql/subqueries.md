# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Subqueries

Sub queries are divided into correlated and uncorrelated subqueries.

1. Uncorrelated subqueries subqueries are very simple, this article will only list some boilerplate code
2. Correlated subqueries, which will be explained in this article

## 1. Uncorrelated subqueries

1. *In* with one column

    ```kt
    sqlClient.createQuery(Book::class) {
        where {
            table.id valueIn subQuery(Author::class) {
                where(table.firstName eq "Alex")
                select(table.books.id)
            }
        }
        select(table)
    }
    ```
    
    For negation logic, you can either use *valueNotIn* or wrap the entire expression by the *not()* function
    
2. *In* with multiple columns

    ```kt
    sqlClient.createQuery(Book::class) {
        where {
            tuple {
                table.name then
                    table.price
            } valueIn subQuery(Book::class) {
                groupBy(table.name)
                select {
                    table.name then
                        table.price.max().asNonNull()
                }
            }
        }
        select(table)
    }
    ```

    For negation logic, you can either use *valueNotIn* or wrap the entire expression by the *not()* function
    
3. Use subquery as simple expression

    ```kt
    sqlClient.createQuery(Book::class) {
        where {
            table.price gt subQuery(Book::class) {
                select(coalesce(table.price.avg(), BigDecimal.ZERO))
            }
        }
        select(table)
    }
    ```
    
4. Use subquery in *select* and *order by* cluase

    ```kt
    sqlClient.createQuery(BookStore::class) {
        
        val subQuery = subQuery(Book::class) {
            where(parentTable.id eq table.store.id)
            select(coalesce(table.price.avg(), BigDecimal.ZERO))
        }
        
        orderBy(subQuery, OrderMode.DESC)
        select {table then subQuery }
    }
    ```
    
    Here, *select* and *order By* use same subquery, you can use local variable to record the subquery and reuse it twice instead of writing the subquery itself twice.
    
5. Use subquery in *any()*

    ```kt
    sqlClient.createQuery(Book::class) {
        where(
            table.id eq any(
                    subQuery(Author::class) {
                    where(table.firstName valueIn listOf("Alex", "Bill"))
                    select(table.books.id)
                }
            )
        )
        select(table)
    }
    ```

6. Use subquery in *some()*

    ```kt
    sqlClient.createQuery(Book::class) {
        where(
            table.id eq some(
                subQuery(Author::class) {
                    where(table.firstName valueIn listOf("Alex", "Bill"))
                    select(table.books.id)
                }
            )
        )
        select(table)
    }
    ```
    
7. Use subquery in *all()*

    ```kt
    sqlClient.createQuery(Book::class) {
        where(
            table.id eq all(
                subQuery(Author::class) {
                    where(table.firstName valueIn listOf("Alex", "Bill"))
                    select(table.books.id)
                }
            )
        )
        select(table)
    }
    ```

## 2. Uncorrelated subqueries



------------------
[< Previous: Contains](./contains.md) | [Back to parent](./README.md) | [Next: Pagination >](./pagination.md)
