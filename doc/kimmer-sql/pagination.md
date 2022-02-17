# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Pagination

## 1. Create count query quickly

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

2. The line with comment β: 

    Call the API specific to kimmer-sql, on the basis of data-query, ignore the *order by* clause and use the new *select* clause to quickly create count-query
    
3. The line with comment γ: 

    Use the result of count-query to determine the paging range, and use data-query to complete the paging query.

> Usage restrictions
>   
>   1. A query created by reselect cannot be further reselected, which will cause an exception.
>   
>   2. If the *select* clause of the original top-level query contains aggregate functions, it will cause an exception.
>   
>   3. If the original top-level query contains a *group by* clause, it will cause an exception.

## 2. Automatic optimization for count-query

In order to make the performance of count query as high as possiblem, kimmer-sql can remove unnecessary table joins of top-level query of count-query.

There are two types of table joins that will not be removed

1. Collection join
    
    Collection table joins for one-to-many or many-to-many associations inevitably affect the number of records, so kimmer-sql keep them forever in count query.

2. Table joins shared by *where* cluase

    This does not need to be explained, only table joins that are **only** used by the old *select* or *order by* clauses may be removed by optimization.
    
Obviously, if the table join does not affect the number of records, it can be removed. there are two possibilities

1. The join type is *left outer join*

3. Although the join type is *inner join*, the foreign key is not null

**Optimization rules**

<table>
    <tr>
        <td rowspan="4">
            AND
        </td>
        <td colspan="2">
            Association type is many-to-one
        </td>
    </tr>
    <tr>
        <td colspan="2">
            Table join is <b>ONLY</b> used by <i>select</i> or <i>order by</i> clause of orginal top-level query
        </td>
    </tr>
    <tr>
        <td rowspan="2">
            OR
        </td>
        <td>
            Join type is LEFT OUTER JOIN
        </td>
    </tr>
    <tr>
        <td>
            The type of many-to-one association is nonnull 
        </td>
    </tr>
</table>

### 2.1 Example for joins only used by *order by* caluse.

```kt
val query = sqlClient.createQuery(Book::class) {
        where(table.price.between(BigDecimal(20), BigDecimal(30)))
        orderBy(table.store.name)    // α
        orderBy(table.name)
        select(table)
    }
val countQuery = query
    .reselect {
        select(table.id.count())
    }
    .withoutSortingAndPaging()
val rowCount = countQuery.execute(con)[0]
```

At the line with comment *α*

1. *table.store* is inner join
2. The association *Book::store* is nullable

This situation cannot be automatically optimized, and the final generated SQL contains this JOIN

```sql
select count(tb_1_.ID) from BOOK as tb_1_ 
inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
where tb_1_.PRICE between $1 and $2
```

Either using left outer join or changing *Book::store* to be non-null can make the automatic optimization take effect. Here, we take using left outer connection as an example.
```kt
val query = sqlClient.createQuery(Book::class) {
        where(table.price.between(BigDecimal(20), BigDecimal(30)))
        orderBy(table.`store?`.name)    
        orderBy(table.name)
        select(table)
    }
val countQuery = query
    .reselect {
        select(table.id.count())
    }
    .withoutSortingAndPaging()
val rowCount = countQuery.execute(con)[0]
```
The generated SQL is
```
select count(tb_1_.ID) from BOOK as tb_1_ where tb_1_.PRICE between $1 and $2
```
    
------------------
[< Previous: Subqueries](./subqueries.md) | [Back to parent](./README.md)
