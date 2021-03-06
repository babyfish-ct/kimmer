# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer](./README.md)/Get started

## 1. Create project

Use Intellij to create a **gradle** project, choose **kotlin/jvm** and **kotlin dsl**
![image](./images/create-project.jpeg)

## 2. Add plugins and dependencies

Edit the "build.gradle.kts" 

Add this section into *plugin{}*
```kts
id("com.google.devtools.ksp") version "1.6.10-1.0.2"
```
Add this section into *dependencies{}*
```kts
implementation("org.babyfish.kimmer:kimmer:0.3.3")
ksp("org.babyfish.kimmer:kimmer-ksp:0.3.3")
```
Then click the refresh icon of gradle window.

![image](./images/gradle-refresh.jpeg)

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

## 4. Execute ksp to generate mutable data model
![image](./images/ksp.jpeg)

Then you will see a new file "ModelDraft.kt" is generated under "build/generated/ksp/main/kotlin", this is mutable data model.

## 5. Consider generated file as source code
Append this section to build.gradle.kts
```kts
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
```
Then click the refresh icon of gradle window.

![image](./images/gradle-refresh.jpeg)

After this step, "build/generated/ksp/main/kotlin" will be considered as a new source folder.

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

    val book2 = new(Book::class).by(book) {
        name += "!"
        store().name += "!"
        for (author in authors()) {
            author.name += "!"
        }
    }

    println("Old object is")
    println(book)
    println("New object is")
    println(book2)
}
```

## 7. Run the app

**Finally, the output is**
```
Old object is
{"authors":[{"name":"child-1"},{"name":"child-2"}],"name":"book","store":{"name":"parent"}}
New object is
{"authors":[{"name":"child-1!"},{"name":"child-2!"}],"name":"book!","store":{"name":"parent!"}}
```

-----------

[Back to home](https://github.com/babyfish-ct/kimmer) | [Back to document](./README.md) | [Next: Dynamics & Unloaded properties >](./dynamic.md)
