# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Containss

## 1. Collection join

In the previous chapter, we discussed table joins in depth

Now let's look at a few joins

1. Join the one-to-many assocaition *BookStore::books*

```kt
sqlClient.createQuery(BookStore::class) {
    where(table.books.name eq "Learnning GraphQL")
    select(table)
}
```

2. Join the many-to-many assocaition *Book::authors*
```kt
sqlClient.createQuery(Book::class) {
    where(table.authors.firstName eq "Alex")
    select(table)
}
```

3. Join the one-to-many assocaition *Auhtor::books*

```kt
sqlClient.createQuery(Author::class) {
    where(table.books.name eq "Learnning GraphQL")
    select(table)
}
```

In this code we can see joins: ```table.books```, ```table.authors```, they target one-to-many associations or many-to-many associations.

**Precise definition**

list = kotlin.collections.List<*>

connection = [org.babyfish.kimmer.graphql.Connection<*>](../../project/kimmer/src/main/kotlin/org/babyfish/kimmer/graphql/Connection.kt)

collection = list + connection

> [org.babyfish.kimmer.graphql.Connection](../../project/kimmer/src/main/kotlin/org/babyfish/kimmer/graphql/Connection.kt) is used to support [graphql-connection](https://relay.dev/graphql/connections.htm), kimmer-sql cannot support it on its own, it must be supported in conjunction with new framework for GraphQL in the future. For now, let's ignore the connection for now.

If a join is created by association whose type is collection *(list + connection)*, it is a collection join. This is the precise definition.

------------------
[< Previous: Table joins](./table-joins.md) | [Back to parent](./README.md) | [Next: Subqueries >](./subqueries.md)
