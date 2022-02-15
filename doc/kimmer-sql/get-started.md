# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Get started

------------------

## 1. First Experience

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
   
   i. "kimmer.draft"
   
      true means to generate source codes of kimmer, its default value is true.

      Let's focus on kimmer-sql, not kimmer. Set it to false.
      
   ii. "kimmer.table"
      
      true means to generate source codes of kimmer, its default value is false.

      We want to used kimmer-sql, so it must be set to true, this is important.
      
   iii. "kimmer.table.collection-join-only-for-sub-query"
   
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
      

[Back to parent](./README.md) | [Next: Null Saftey >](./null-safety.md)

