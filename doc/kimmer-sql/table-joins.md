# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Table joins

In this article, we will touch on four concepts

1. Merged join
2. Phantom join
3. Half join
4. Reverse join

## 1. Merged join

In kimmer-sql, you can create temporary table joins at will without having to use local variables to remember table joins. Temporary table joins can be created anywhere in SQL.

More importantly, you don't have to consider whether there is some conflict between these temporary table joins.

This feature makes kimmer-sql very suitable for dynamic SQL

```kt
// Environment arguments
val sqlClient = ... some code to get sql client ...
val con = ... some code to get JDBC/R2DBC connection ...

// Dynamic query arguments
name: String? = ...Some code...
val bookName: String? = 
val storeName: String?
val inclusiveStoreIds: Collection<UUID>? =
exclusiveStoreIds: Collection<UUID>? =
orderByName: Boolean,
orderByBookName: Boolean,
orderByStoreName: Boolean

// Now, start dynamic query
name?.let {
    where { table.fullName ilike it }
}

bookName?.let {
    where { table.books.name ilike it }    // α
}

storeName?.let {
    where { table.books.store.name ilike it }   // β
}

inclusiveStoreIds
    ?.takeIf { it.isNotEmpty() }
    ?.let {
        where { table.books.store.id valueIn it }   // γ
    }

exclusiveStoreIds
    ?.takeIf { it.isNotEmpty() }
    ?.let {
        where { table.books.store.id valueNotIn it }   // δ
    }

if (orderByName) {
    orderBy(table.fullName)
}

if (orderByBookName) {
    orderBy(table.`books?`.name)   // ε
}

if (orderByStoreName) {
    orderBy(table.`books?`.`store?`.name)   // ζ
}

select(table.id)
    .distinct() // Joining to list association
                // cause duplicated data, distinct
}

// Get the result
val matchedAuthorIds = query.execute(con)
```

------------------
[< Previous: Null safety](./null-safety.md) | [Back to parent](./README.md) | [Next: Contains >](./contains.md)
