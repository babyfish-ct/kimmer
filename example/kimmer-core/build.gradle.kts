plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

group = "org.babyfish.kimmer.example"
version = "0.2.9"

repositories {
    mavenCentral()
}333

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.babyfish.kimmer:kimmer:0.2.9")
    ksp("org.babyfish.kimmer:kimmer-ksp:0.2.9")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}