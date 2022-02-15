plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

group = "org.babyfish.kimmer.example"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.babyfish.kimmer:kimmer-sql:0.1.3")
    ksp("org.babyfish.kimmer:kimmer-ksp:0.1.3")
}

ksp {
    arg("kimmer.draft", "false")
    arg("kimmer.table", "true")
}


