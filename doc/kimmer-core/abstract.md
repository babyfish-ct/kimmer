# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer](./README.md)/Abstract 

The following code is the simplest, but not the best
```kt
interface BookStore: Immutable {
    val name: String
    val books: List<Book>
}

interface Book: Immutable {
    val name: String
    val store: BookStore?
    val authors: List<Author>
}

interface Author: Immutable {
    val name: String
    val books: List<Book>
}
```

All three types have a "name" property, a better way is to use inheritance.

```kt

interface NamedObj: Immutable {
    val name: String
}

interface BookStore: NamedObj {
    val books: List<Book>
}

interface Book: NamedObj {
    val store: BookStore?
    val authors: List<Author>
}

interface Author: NamedObj {
    val books: List<Book>
}
```

The NamedObj here is just to reduce duplication of code, it's not a valid business entity, but, by default, you can create objects of it.

```kt
val namedObject = new(NamedObj::class).by {}
```

This is obviously unreasonable. You can add @Abstract annotation to NamedObj interface
```kt
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.Abstract

@Abstract
interface NamedObj: Immutable {
    val name: String
}
```

Now, trying to create an object of type NamedObj will result in a compile error!

[< Previous: Jackson](jackson.md) | [Back to document](./README.md) | [Coroutine >](coroutine.md)
