kimmer-sql is a SQL DSL to help you access RDBMS by kotlin, whether based on JDBC or R2DBC.

# 1. Why?

There are currently many solution for accessing RDMBS

1. JPA
2. Mybatis
3. JOOQ
4. Exposed
5. Ktorm

Why provide a new SQL DSL?

1. Dynamic table joins
   While many data accessing solutions support dynamic queries, they all only let you generate dynamic where conditions. This is not enough, each dynamic filter condition may depend on some table joins, and even worse, different dynamic dynamic filter conditions may contain the same tabe join paths. It is difficult to write complex dynamic queries without dynamic table joins(Dynamic SQL is the primary design goal of this kimmer-sql).

2. Smart SQL Optimization
   kimmer-sql will not mechanically map kotlin code to SQL, it can automatically remove unnecessary complexity from user code that has no effect on query results, and finally generate the most simple and high-performance SQL possible.

3. Null safety
   Among the above solutions, JOOQ is the best at finding SQL errors at the compile stage, but JOOQ is designed for Java rather than kotlin. Kotlin is a language with null safety, this DSL expects the SQL model to have the same null safety as kotlin.

# 2. Documentation

--------------------

[Home](https://github.com/babyfish-ct/kimmer)
