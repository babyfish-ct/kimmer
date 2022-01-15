plugins {
    id("org.jetbrains.dokka") version "1.6.10"
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.2")
    implementation("com.squareup:kotlinpoet:1.10.2")

    dokkaHtmlPlugin("org.jetbrains.dokka:dokka-base:1.6.10")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType(Jar::class) {
        if (archiveClassifier.get() == "javadoc") {
            dependsOn(dokkaHtml)
            from("build/dokka/html")
        }
    }
}