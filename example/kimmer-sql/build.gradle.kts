plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

group = "org.babyfish.kimmer.sql.example"
version = "0.3.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.babyfish.kimmer:kimmer-sql:0.3.3")
    ksp("org.babyfish.kimmer:kimmer-ksp:0.3.3")
    runtimeOnly("com.h2database:h2:2.1.210")
}

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
