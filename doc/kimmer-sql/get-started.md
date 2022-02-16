# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Get started

1. Use Intellij to create a **gradle** project, choose **kotlin/jvm** and **kotlin/dsl**
   ![image](../kimmer-core/images/create-project.jpeg)

2. Edit the "build.gradle.kts" 

   a. Add this section into **plugin{}**
      ```
      id("com.google.devtools.ksp") version "1.6.10-1.0.2"
      ```
   
   b. Add this section into **dependencies{}**
   
      ```
      implementation("org.babyfish.kimmer:kimmer-sql:0.1.5")
      ksp("org.babyfish.kimmer:kimmer-ksp:0.1.5")
      ```
   
   c. Add this section into as toppest declaration
      ```
      ksp {
          arg("kimmer.draft", "false")
          arg("kimmer.table", "true")
          arg("kimmer.table.collection-join-only-for-sub-query", "true")
      }
      kotlin {
          sourceSets.main {
              kotlin.srcDir("build/generated/ksp/main/kotlin")
          }
      }
      ```
   
   We configured 3 arguments to ksp.
   
   - "kimmer.draft"
   
      true means to generate source codes of kimmer, its default value is true.

      Let's focus on kimmer-sql, not kimmer. Set it to false.
      
   - "kimmer.table"
      
      true means to generate source codes of kimmer, its default value is false.

      We want to used kimmer-sql, so it must be set to true, this is important.
      
   - "kimmer.table.collection-join-only-for-sub-query"
   
      This argument will be discussed in detail in Chapter "[Contains](../contains.md)", it is recommended to open.
      
3. Refresh gradle to download all dependencies and plugins

   ![image](../kimmer-core/images/gradle-refresh.jpeg)

4. Add data model interfaces
   
   Create a package with any path， add threes kotlin files into it

   a. BookStore.kt
   
      ```kt
      package org.babyfish.kimmer.sql.example.model

      import org.babyfish.kimmer.sql.Entity
      import java.util.*

      interface BookStore: Entity<UUID> {
          val name: String
          val books: List<Book>
      }
      ```
      
   b. Book.kt
   
      ```kt
      package org.babyfish.kimmer.sql.example.model

      import org.babyfish.kimmer.sql.Entity
      import java.math.BigDecimal
      import java.util.*

      interface Book: Entity<UUID> {
          val name: String
          val store: BookStore
          val edition: Int
          val price: BigDecimal
          val authors: List<Author>
      }
      ```
      
   c. Author.kt
   
      ```kt
      package org.babyfish.kimmer.sql.example.model

      import org.babyfish.kimmer.sql.Entity
      import java.util.*

      interface Author: Entity<UUID> {
          val firstName: String
          val lastName: String
          val fullName: String
          val books: List<Book>
      }
      ```
      
   Two points to note

   - All database entities must extend *org.babyfish.kimmer.sql.Entity*， its generic parameter must be specified as the type of priamry key, but the business entities itself cannot have generic parameters.
   - Unlike kimmer, kimmer-sql does not allow multiple entity types to be defined in one source code file. Otherwise, the kimmer-ksp precompiler will report an error.

5. Let kimmer-ksp generates extra sources code.

   ![image](../kimmer-core/images/ksp.jpeg)
   
   Remember we had such a configuration in gradle before?
   
   ```
   kotlin {
       sourceSets.main {
           kotlin.srcDir("build/generated/ksp/main/kotlin")
       }
   }
   ```
   
   It must be ensured that the automatically generated code can be recognized by intellij, otherwise the IDE will not be able to perform intellisense in the subsequent development process.
   
   
6. Add other code and run

   You also need to create three files, which can be learned or copied from the example of this project. To save space, the code will not be shown here.
   
   - [example/kimmer-sql/src/main/resources/database.sql](../../example/kimmer-sql/src/main/resources/database.sql)
   
      This file is used to initialize database, create tables and insert data.
      
   - [example/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/example/AppContext.kt](../../example/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/example/AppContext.kt)

      This file used to setup environment
      
      - Uses "database.sql" to create tables and insert data
      - Specify ORM meta information for the kimmer-ksp

   - [example/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/example/App.kt](../../example/kimmer-sql/src/main/kotlin/org/babyfish/kimmer/sql/example/App.kt)
      
      This file is what kimmer-sql wants to present to the user, it's a small but comprehensive example. including
      
      - Dynamic sql
      - Table join
      - Sub quieres
      - Mix dsl and native sql together
      - Pagination

------------------------------

[Back to parent](./README.md) | [Next: Null Saftey >](./null-safety.md)

