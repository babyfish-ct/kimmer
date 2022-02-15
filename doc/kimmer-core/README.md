# kimmer

Port [https://github.com/immerjs/immer](https://github.com/immerjs/immer) for kotlin/jvm *(requires kotlin 1.5+)*.

> *Immer is the winner of the "Breakthrough of the year" React open source award and "Most impactful contribution" JavaScript open source award in 2019.*
> 
> *It's simple and powerful, so I bring its design pattern for kotlin/jvm.*

**Create the next immutable state tree by simply modifying the current tree.**

> *If data tree is shallow, kotlin data class's copy function is very simple, but if the data tree is deep, copy function is no longer convenient and kimmer can help you.* 
> 
> *If you're confused by this, please click [here](./value.md) to find out why.* 

## 1. Usage

1. Create immutable object from scratch
```kt
val book = new(Book::class).by {
    name = "book"
    store().name = "parent"
    authors() += new(Author::class).by {
        name = "child-1"
    }
    authors() += new(Author::class).by {
        name = "child-2"
    }
}
```

2. Create immutable object based on old immutable object(it looks like modification, it's the core value of this framework)
```kt
val book2 = new(Book::class).by(book) {
    name += "!"
    store().name += "!"
    for (author in authors()) {
        author.name += "!"
    }
}
```

> For the modification, it looks like the copy-on-write strategy of linux "fork", **unchaged parts are always shared and reused**.

# Documatation

--------------------------

[Home page](https://github.com/babyfish-ct/kimmer) | [kimmer-sql](../kimmer-sql/README.md)
