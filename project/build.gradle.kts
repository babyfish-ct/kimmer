plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("maven-publish")
    id("signing")
}

allprojects {
    group = "org.babyfish.kimmer"
    version = "0.1.5"
}

subprojects {

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
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
            artifactId = "kimmer-parent"
            pom {
                name.set("kimmer")
                description.set("Parent project for kimmer")
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
