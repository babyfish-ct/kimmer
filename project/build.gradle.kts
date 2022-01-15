plugins {
    kotlin("jvm") version "1.6.10" apply false
    id("maven-publish")
    id("signing")
}

allprojects {
    group = "org.babyfish.kimmer"
    version = "0.0.0"
}

subprojects {

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }
}
