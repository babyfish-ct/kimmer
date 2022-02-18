# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Non good design to avoid intellij's bug

Since 0.1.7, the select function can be used by two ways

1. This way can only be used to select one field
    ```kt
    sqlCient.createQuery(YourEntity::class) {
    
        ...where, groupBy, having, orderBy...
        
        select(table)
    }
    ```
    
2. This way can only be used to select two or more fields
    ```kt
    sqlCient.createQuery(YourEntity::class) {
    
        ...where, groupBy, having, orderBy...
        
        select {
            table then
            table.association1 then
            table.association2 then
            table.association3.name
        }
    }
    ```
    
Obviously, these are two incompatible ways of writing
 
In 0.1.6, the old design style is (Use *TypedRootQuery* to be example)
 
```kt
fun <R> select(
    R: Selection<T>
): TypedRootQuery<E, ID, R>

fun <A, B> select(
    selection1: Selection<A>, 
    selection1: Selection<B>
): TypedRootQuery<E, ID, Pair<A, B>>

fun <A, B, C> select(
    selection1: Selection<A>, 
    selection2: Selection<B>, 
    selection3: Selection<C>
): TypedRootQuery<E, ID, Tripple<A, B, C>>

fun <T1, T2, T3, T4> select(
    selection1: Selection<T1>, 
    selection2: Selection<T2>, 
    selection3: Selection<T3>
    selection4: Selection<T4>
): TypedRootQuery<E, ID, Tuple4<T1, T2, T3, T4>>

...

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9> select(
    selection1: Selection<T1>, 
    selection2: Selection<T2>, 
    selection3: Selection<T3>,
    selection4: Selection<T4>,
    selection5: Selection<T5>,
    selection6: Selection<T6>,
    selection7: Selection<T7>,
    selection8: Selection<T8>,
    selection9: Selection<T9>,
): TypedRootQuery<E, ID, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>>
```

With this design, no matter how many fields you want to select, there is no difference in how you use them

But, this will cause problem of Intellij, for example

```kt
sqlClient.createQuery(YourEntity::class) {

    ...where, groupBy, having, orderBy...
    
    select(
        // You just wrote the select function without any parameters, 
        // you enter the first letter here and expect Intellij's intellisense
    )
}
```

The intellij will be frozen, UI interface is not responding. 

Eventually, after waiting long enough, OS will ask you if you need to force terminate it.

*(Platform, Apple M1)*

-----------------


[< Previous: Out of scope](./out-of-scope.md) | [Back to parent](./README.md)
 
