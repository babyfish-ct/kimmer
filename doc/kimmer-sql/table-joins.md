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
val name: String? = ...Some code...
val bookName: String? = ...Some code...
val storeName: String? = ...Some code...
val inclusiveStoreIds: Collection<UUID>? = ...Some code...
exclusiveStoreIds: Collection<UUID>? = ...Some code...
orderByName: Boolean = ...Some code...
orderByBookName: Boolean = ...Some code...
orderByStoreName: Boolean = ...Some code...

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

> In order for this code to be compiled correctly, the ksp parameter *kimmer.table.collection-join-only-for-sub-query* needs to be set to *false*. For details, please refer to [Contains](./contains.md). Let's ignore this ksp parameter for now and focus on the table joins now

In the code above

1. The line with comment *α*: 

    ```table.books```
    Inner join chain, 1 entity type is joined
    
2. The line with comment *β*: 

    ```table.books.store```
    Inner join chain, 2 entity types are joined

3. The line with comment *γ*: 

    ```table.books.store```
    Inner join chain, 2 entity types are joined

4. The line with comment *δ*: 

    ```table.books.store```
    Inner join chain, 2 entity types are joined
    
5. The line with comment *ε*: 

    ```table.`books?` ```
    Left outer join chain, 1 entity type is joined
    
6. The line with comment *ζ*: 

    ```table.`books?`.`store?` ```
    Left outer join chain, 2 entity types are joined
    
> In fact, γ and δ are not complete table joins, they are half joins, which will be described later. You can think for now that kimmer-sql foolishly treats them as normal table joins. 

Obviously, for the code above, these temporary table joins will conflict if multiple dynamic conditions are met at runtime. kimmer-sql can merge several join paths together and remove redundant connections.

#### Path type merge rule

Let's look at the following 3 table join paths

```
a->b->c->d->e->f->g
```
```
a->b->c->h->i->j
```
```
a->x->y->z->a->b->c->d
```

kimmer-sql will merge these paths into a tree

```
-+-a
 |
 +----+-b
 |    |
 |    \----+-c 
 |         |
 |         +----+-d
 |         |    |
 |         |    \----+-e
 |         |         |
 |         |         \----+-f
 |         |              |
 |         |              \------g
 |         |
 |         \----+-h
 |              |
 |              \----+-i
 |                   |
 |                   \------j
 |
 \----+-x
      |
      \----+-y
           |
           \----+-z
                |
                \----+-a
                     |
                     \----+-b
                          |
                          \----+-c
                               |
                               \------d
```
This tree removes all redundant table joins, and kimmer-sql will generate the final SQL based on this tree

#### Join style merge rule

In the above code, we not only used inner join ```table.books.store``` but also left outer join ```table.`books?`.`store?` ```.

kimmer-sql merges them like this

1. If all the conflicting table joins are left outer joins, the left outer join is finally adopted.
2. If any one of the conflicting table joins is an inner join, the inner join is finally adopted.

## 2. Phantom join

Phantom join is a very simple concept, just compare it with normal join to understand. 

Let's first look at an example of a normal table join

```kt
val sqlClient = ... some code to get sql client ...
val con = ... some code to get JDBC/R2DBC connection ...

val query = sqlClient.createQuery(Book::class) {
    where { table.store.name eq "MANNING" }
    select(table)
}
val rows = query.execute(con)
```

We use table join ```table.store```, this code generates the following SQL
```sql
select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
from BOOK as tb_1_ 
inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID 
where tb_2_.NAME = ?
```
In the final generated SQL, we see SQL join ```inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID```.

Now let's look at the phantom table join.

```kt
val sqlClient = ... some code to get sql client ...
val con = ... some code to get JDBC/R2DBC connection ...

val query = sqlClient.createQuery(Book::class) {
    where { table.store.id eq UUID.fromString("2fa3955e-3e83-49b9-902e-0465c109c779") }
    select(table)
}
query.execute(con)
```
We use table join ```table.store```, this code generates the following SQL
```
select tb_1_.ID, tb_1_.EDITION, tb_1_.NAME, tb_1_.PRICE, tb_1_.STORE_ID 
from BOOK as tb_1_ 
where tb_1_.STORE_ID = ?
```
We don't see any SQL joins, we only see condition ```tb_1_.STORE_ID = ?``` for foreign keys.

For many-to-one association based on foreign key. The id of the parent table is actually the foreign key of the child table.

So, if we join an association based on a foreign key but don't access any fields of the associated object other than id, the join will be treated as a phantom join. It looks like table join in kotlin code, but nothing is generated in SQL.

------------------
[< Previous: Null safety](./null-safety.md) | [Back to parent](./README.md) | [Next: Contains >](./contains.md)
