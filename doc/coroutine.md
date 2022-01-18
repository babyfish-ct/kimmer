# [kimmer](https://github.com/babyfish-ct/kimme)/[document](README.md)/Coroutine

In the previous examples, we have been using the new function

```kt
// Create from scratch
val book = new(Book::class).by {
    ...blabla...
}

// Create based on existsing object(Looks like modification) 
val book2 = new(Book::class).by(book) {
    ...blabla...
}
```

Kimmer supports the "newAsync" function, the usage is the same as above, the only difference is that the labmda expression is a suspend function.

> To use "newAsync", please add the dependencies about kotlin coroutines by yourself.

```kt
suspend fun executeAsync() {

    // org.babyfish.kimmer.newAsync
    val book2 = newAsync(Book::class).by(book) {
 
        delay(100)
        name += "!"

        for (author in authors()) {
             author.name += "!"
             delay(100)
        }

        // org.babyfish.kimmer.AsyncDraft.newAsync
        store = newAsync(BookStore::class).by {
            delay(100)
            name = "New store"
        }
    }
}
```

> Note
> 
> Draft for newAsync is threadsafe

----

[< Previous: Abstract](abstract.md) | [Back to document](README.md) | [Back to home](https://github.com/babyfish-ct/kimme)
