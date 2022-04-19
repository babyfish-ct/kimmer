plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
}

group = "org.babyfish.kimmer.example"
version = "0.3.3"

repositories {
    mavenCentral()
}333

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.babyfish.kimmer:kimmer:0.3.3")
    ksp("org.babyfish.kimmer:kimmer-ksp:0.3.3")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}