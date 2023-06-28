import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(testLibs.plugins.spring.boot)
    alias(testLibs.plugins.dependency.management)
    alias(testLibs.plugins.kotlin.jvm)
    alias(testLibs.plugins.kotlin.spring)
    alias(testLibs.plugins.ktlint)
}

dependencyManagement {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES) {
            // override version of selenium used
            bomProperty("selenium.version", testLibs.versions.selenium.get())
        }
    }
}

dependencies {
    implementation(testLibs.spring.boot.starter.oauth2.client)
    implementation(testLibs.spring.boot.starter.web)

    testImplementation(project(":lib"))
    testImplementation(testLibs.bundles.kotest)
    testImplementation(testLibs.spring.boot.starter.test)
    testImplementation(testLibs.testcontainers.keycloak)

    testImplementation(testLibs.selenium)
    testImplementation(testLibs.bundles.alkemy)
}
