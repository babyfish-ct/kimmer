# kimmer
Port "https://github.com/immerjs/immer" for kotlin/jvm

## Usage

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

3. Create immutable object based on old immutable object(it looks like modification, it's the core value of this framework)
```kt
val book2 = new(Book::class).by(book) {
    name += "!"
    store().name += "!"
    for (author in authors()) {
        author.name += "!"
    }
}
```

## Step by step guide
Please click [here](doc/get-started.md) to view the guide

## Note
If your JVM version >= 16, you will get an exception
```
java.lang.reflect.InaccessibleObjectException: Unable to make protected final java.lang.Class java.lang.ClassLoader.defineClass(byte[],int,int) throws java.lang.ClassFormatError accessible: module java.base does not "opens java.lang" to unnamed module 
```
In order to resolve this problem, please add 
```
--illegal-access=permit
```
to JVM arguments

