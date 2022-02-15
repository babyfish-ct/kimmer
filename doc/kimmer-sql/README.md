# kimmer-sql

kimmer-sql is a SQL DSL to help you access RDBMS by kotlin, whether based on JDBC or R2DBC.

## 1. Why?

There are currently many solutions for accessing RDMBS

1. JPA
2. Mybatis
3. JOOQ
4. Exposed
5. Ktorm

Why provide a new SQL DSL?

1. **Dynamic table joins**

   While many data accessing solutions support dynamic queries, they all only let you generate dynamic where conditions. This is not enough, each dynamic filter condition may depend on some table joins, and even worse, different dynamic dynamic filter conditions may contain the same tabe join paths. It is difficult to write complex dynamic queries without dynamic table joins.
   
   > Dynamic SQL is the primary design goal of this kimmer-sql

2. **Smart SQL Optimization**

   kimmer-sql will not mechanically map kotlin code to SQL, it can automatically remove unnecessary complexity from user code that has no effect on query results, and finally generate the most simple and high-performance SQL possible.

3. **Null safety**

   Among the above solutions, JOOQ is the best at finding SQL errors at the compile stage, but JOOQ is designed for Java rather than kotlin. Kotlin is a language with null safety, this DSL expects the SQL model to have the same null safety as kotlin.

4. **Powerful paging API**

   Paging query requires two SQL statements, one for querying the number of records and one for data of a page, let's call them **count-query** and **data-query**. These two SQL statements have both the same parts and different parts, it is difficult to share the code unless that SQL logic is very simple.

   > For example, **count-query** does not require "order by" clause but **data-query requires**. If there are some table joins in "order by" clause, are these table joins only used by "order by" clause or also be used by other parts of SQL? If a table join is only used by "order by" caluse, is it guaranteed not to affect the number of records so that it can be removed from the **count-query**?

   kimmer-ksp provides new APIs and optimization algorithms to perfectly solve this problem.

## 2. Documentation

1. [Get started](./get-started.md)
2. [Null safety](./null-safety.md)
3. [Table Joins(merged join, phantom Join, half join, reverse join)](../table-joins.md)
4. [Contains](./contains.md)
5. [Subqueries](./subqueries.md)
6. [Pagination](./pagination.md)

--------------------

[Home](https://github.com/babyfish-ct/kimmer)
