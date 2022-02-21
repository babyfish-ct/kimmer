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
    
    1. Here, *select* and *order By* use same subquery, you can use local variable to record the subquery and reuse it twice instead of writing the subquery itself twice.
    2. This is not uncorrelated subquery, it's correlated subquery because its ues *parentTable*, which will be discussed later.
    
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

## 2. Correlated subqueries

### 2.1. *parentTable*

In the lambda expression of the subquery function, not only the implicit object *table* is provided to represent the table of the subquery itself, but also the implicit object **parentTable** is provided to represent the table of the parent query.

```kt
sqlClient.createQuery(Book::class) {
    where {
        exists(subQuery(Author::class) {
            where(
                parentTable.id eq table.books.id, // α
                table.firstName eq "Alex"
            )
            select(constant(1)) // β
        })
    }
    select(table) 
}
```
1. At the line with comment *α*, *parentTable* means the root table of parent query.
2. At the line with comment *β*, use select the *constant(1)* because *exists()* does not care the return format of subquery.

> The difference between *value()* and *constant()*
> 
> 1. *value()* implants variables into SQL in the form of JDBC/R2DBC parameters. For JDBC, the parameter placeholder is *?*, and for R2DBC, the parameter placeholder is *$1*, *$2*, ...
> 
> 2. *constant()* is hard-coded into SQL, which is helpful for functional indexes. In order to avoid injection attacks, constant only accepts numeric types.

In fact, kimmer-sql has a special treatment for exists, no matter what you select, it will be replaced with *select 1*

```kt
sqlClient.createQuery(Book::class) {
    where {
        exists(subQuery(Author::class) {
            where(
                parentTable.id eq table.books.id, 
                table.firstName eq "Alex"
            )
            select(table) // α
        })
    }
    select(table) 
}
```

At the line with comment *α*, *table.\** is selected, but there is still *select 1* in generated SQL, like this
```sql
select 
    tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
from BOOK as tb_1_ 
where exists(
    select 1 from AUTHOR as tb_2_ 
    inner join BOOK_AUTHOR_MAPPING as tb_3_ on tb_2_.ID = tb_3_.AUTHOR_ID 
    where 
        tb_1_.ID = tb_3_.BOOK_ID 
    and 
        tb_2_.FIRST_NAME = $1
)
```

### 2.2 Untyped subquery

Since *exists()* does not care about the return format of the subquery, *kimmer-sql* supports an *untypedSubQuery* function that allows developers to create an untyped subquery.

- Developers do not need to specify select clauses for untyped subqueries
- The only function of untyped subqueries is to work with *exists()*

```kt
sqlClient.createQuery(Book::class) {
    where {
        exists(untypedSubQuery(Author::class) {
            where(
                parentTable.id eq table.books.id,
                table.firstName eq "Alex"
            )
            // α
        })
    }
    select(table)
}
```
At the line of comment *α*, there is not *select clause* for untyped subquery.

------------------
[< Previous: Contains](./contains.md) | [Back to parent](./README.md) | [Next: Pagination >](./pagination.md)
