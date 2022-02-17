# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Pagination

## 1. reselect & withoutSoringAndPaging

Paging query requires two SQL statements, one for querying the number of records and one for data of a page, let's call them count-query and data-query. 

These two SQL statements have both the same parts and different parts, it is difficult to share the code unless that SQL logic is very simple.

Developers only need to write data-query, kimmer-sql automatically creates count-query.

```kt

// α
val query = sqlClient.createQuery(Book::class) {

    ...Some code for dynamic table joins, dynamic filters and dynamic orders ...
    
    select(table)
}

// β
val countQuery = query
    .reselect {
        select(table.id.count())
    }
    .withoutSortingAndPaging()

// γ
val rowCount = countQuery.execute(con)[0]
val limit = ...use rowCount to calculate limit...
val offset = ...use rowCount to calculate offset...
val rows = query.limit(limit, offset).execute(con)
```


1. The line with comment α: 

    Developer creates data query.

3. The line with comment β: 

    Call the API specific to kimmer-sql, on the basis of data-query, ignore the *order by* clause and use the new *select* clause to quickly create count-query
    
5. The line with comment γ: 

    Use the result of count-query to determine the paging range, and use data-query to complete the paging query.


------------------
[< Previous: Subqueries](./subqueries.md) | [Back to parent](./README.md)
