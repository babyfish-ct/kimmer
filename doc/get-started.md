# Step by step guid

## 1. Create project

Use Intellij to create an project, choose kotlin/jvm and kotlin dsl
[image](./create-project.jpeg)

##. 2. Edit the build.gradle.kts 
a. Add this section into *plugin{}*
```kts
id("com.google.devtools.ksp") version "1.6.10-1.0.2"
```
b. Add this section into *dependencies{}*
```kts
implementation("org.babyfish.kimmer:kimmer:0.0.0")
ksp("org.babyfish.kimmer:kimmer-ksp:0.0.0")
```
Then click the refresh icon of gradle window.
[image](./gradle-refresh.jpeg)

## 3. Define your immutable data interfaces
Create a new file "Model.kt" under src/main/kotlin
```kt
import org.babyfish.kimmer.Immutable

interface BookStore: Immutable {
    val name: String
    val books: List<Book>
}

interface Book: Immutable {
    val name: String
    val store: BookStore
    val authors: List<Author>
}

interface Author: Immutable {
    val name: String
    val books: List<Book>
}
```

## 4. Execute ksp
[image](./ksp.jpeg)
Then you will see an new file "ModelDraft.kt" is generated under "build/generated/ksp/main/kptlin", this is mutable data model.

## 5. Add generated source code
Append this section to build.gradle.kts
```
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
```
Then click the refresh icon of gradle window.
[image](./gradle-refresh.jpeg)
After this step, "build/generated/ksp/main/kotlin" will be considered as another source folder.

## 6. Add main function
Create a new file "App.kt" under src/main/kotlin
```kt
import org.babyfish.kimmer.new

fun main(args: Array<String>) {
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
    println("Old object is")
    println(book)

    val book2 = new(Book::class).by(book) {
        name += "!"
        store().name += "!"
        for (author in authors()) {
            author.name += "!"
        }
    }
    println("New object is")
    println(book2)
}
```

## 7. Run the app
If your JVM version >= 16, you will get an exception
```
java.lang.reflect.InaccessibleObjectException: Unable to make protected final java.lang.Class java.lang.ClassLoader.defineClass(byte[],int,int) throws java.lang.ClassFormatError accessible: module java.base does not "opens java.lang" to unnamed module 
```
In order to resolve this problem, please add 
```
--illegal-access=permit
```
to JVM arguments
[image](./vm-args.jpeg)

The result is
```
Old object is
{"authors":[{"name":"child-1"},{"name":"child-2"}],"name":"book","store":{"name":"parent"}}
New object is
{"authors":[{"name":"child-1!"},{"name":"child-2!"}],"name":"book!","store":{"name":"parent!"}}
```
