plugins {
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    id("org.jetbrains.dokka") version "1.6.10"
}

dependencies {
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")

    implementation("org.springframework.boot:spring-boot-autoconfigure:2.6.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")

    testImplementation(kotlin("test"))

    kspTest(project(":kimmer-ksp"))
    dokkaHtmlPlugin("org.jetbrains.dokka:dokka-base:1.6.10")
}

kotlin {
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

java {
    withSourcesJar()
}