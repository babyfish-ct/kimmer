# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Out of scope

Since the project has just started, this article lists features that have not yet been developed in the first version.

Among these to-do options

1. Some are temporarily out of energy to do, although not now, but there may be in the future
2. Some are not allowed by design and will never be

## 1. Association fetching like ORM

There is currently no ability fetch associated objects of queried object, which is why kimmer-sql claims to be a SQL DSL rather than an ORM.

> But there is one exception, for a many-to-one relationship implemented based on foreign keys, you will get an associated kimmer object with no fields other than id, because this is zero cost.

In fact, it doesn't make sense to fetch the associated objects of the object fixedly in the data layer. A better way is to dynamically fetch the associated objects in GraphQL according to the client's needs. (In fact, kimmer-sql is the underlying framework for my future GraphQL framework)

Of course, after building the upper-level GraphQL framework, I may think, without using GraphQL, how to make kimmer-sql as powerful as GraphQL to dynamically fetch  associated objects.

In any case, either there is none, or there is a stronger and smarter solution.

## 2. Modification

Now, all functions are queries, insert/update/delete are not discussed in the documentation and examples.

This feature will definitely be available in the future. In fact, the GraphQL framework I will develop based on kimmer-sql needs a very powerful and intelligent modification function.

## 3. TransactionManager

The current implementation is very lightweight and does not have any transaction management related functionality other than executing the automatically generated SQL on the JDBC/R2DBC connection.

This feature will never be available. In the field of transaction management, Spring/SpringBoot is the most powerful and elegant implementation, whether for JDBC or R2DBC. The SQL DSL framework provides its own transaction management mechanism, which has no benefit other than increasing the developer's learning burden and making Spring integration more inconvenient.

## 4. Right/Full outer join

If you are careful, you may find that all the documents discuss inner join and left outer join, but never mention right join and full join.

This is not allowed by design.

A notable feature of kimmer-sql is the same null safety as kotlin, inner join indicates that the target table is not null, and left join indicates that the target table is nullable, but in any case, inner join and left join cannot change the nullability of the current table so that it is possible to design API supports null safety. However, right join and full join can change the nullability of the current table, and designing null-safe API will no longer be possible.

Right join is not difficult to handle, it only needs developers to make a workaround in SQL writing.

The real trouble is full join, but I think that full join is mostly used in OLAP field, and OLTP field where developers are highly involved should be rarely used, even if OLTP project needs full join in some occasions, it is handled as native sql, Just bypass the framework.

Compared to Null safety, I think Null safety for SQL is more important.

------------------

[< Previous: Pagination](./pagination.md) | [Back to parent](./README.md) | [Not good design to avoid intellij's bug >](./intellij-bug.md)
