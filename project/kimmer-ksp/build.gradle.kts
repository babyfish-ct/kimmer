plugins {
    kotlin("jvm") version "1.6.10"
}

group = "org.babyfish.kimmer"
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.2")
    implementation("com.squareup:kotlinpoet:1.10.2")
}
