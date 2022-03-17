plugins {
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    id("org.jetbrains.dokka") version "1.6.10"
    id("maven-publish")
    id("signing")
}

dependencies {

    api(project(":kimmer"))
    api("io.r2dbc:r2dbc-spi:0.9.1.RELEASE")

    kspTest(project(":kimmer-ksp"))

    testImplementation(kotlin("test"))
    testImplementation("org.springframework:spring-jdbc:5.3.16")
    testImplementation("com.fasterxml.uuid:java-uuid-generator:4.0.1")

    testImplementation("com.h2database:h2:2.1.210")
    testRuntimeOnly("io.r2dbc:r2dbc-h2:0.9.1.RELEASE")
    testImplementation("mysql:mysql-connector-java:8.0.28")
    testRuntimeOnly("dev.miku:r2dbc-mysql:0.8.2.RELEASE")
    testImplementation("org.postgresql:postgresql:42.3.0")
    testRuntimeOnly("org.postgresql:r2dbc-postgresql:0.9.0.RELEASE")
}

ksp {
    arg("kimmer.table", "true")
    arg("kimmer.table.collection-join-only-for-sub-query", "false")
}

kotlin {
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
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

// Publish to maven-----------------------------------------------------
val NEXUS_USERNAME: String by project
val NEXUS_PASSWORD: String by project

publishing {
    repositories {
        maven {
            credentials {
                username = NEXUS_USERNAME
                password = NEXUS_PASSWORD
            }
            name = "MavenCentral"
            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = "kimmer-sql"
            from(components["java"])
            pom {
                name.set("kimmer")
                description.set("SQL DSL base on kotlin")
                url.set("https://github.com/babyfish-ct/kimmer")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/babyfish-ct/kimmer/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("babyfish-ct")
                        name.set("陈涛")
                        email.set("babyfish.ct@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/babyfish-ct/kimmer.git")
                    developerConnection.set("scm:git:ssh://github.com/babyfish-ct/kimmer.git")
                    url.set("https://github.com//babyfish-ct/kimmer")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
