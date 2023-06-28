// Kotlin Gradle plugin must be loaded in the parent/root project
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
