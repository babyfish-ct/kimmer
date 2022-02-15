# [Home](https://github.com/babyfish-ct/kimmer)/[kimmer-sql](./README.md)/Get started

------------------

## 1. First Experience

1. Use Intellij to create a **gradle** project, choose **kotlin/jvm** and **kotlin/dsl**
   ![image](../kimmer-core/images/create-project.jpeg)

2. Edit the "build.gradle.kts" 

   a. Add this section into plugin{}
   ```
   id("com.google.devtools.ksp") version "1.6.10-1.0.2"
   ```
   
   b. Add this section into dependencies{}
   
   ```
   implementation("org.babyfish.kimmer:kimmer-sql:0.1.5")
   ksp("org.babyfish.kimmer:kimmer-ksp:0.1.5")
   ```

[Back to parent](./README.md) | [Next: Null Saftey >](./null-safety.md)

