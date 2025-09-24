plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
    alias(testLibs.plugins.kotest)
}

dependencies {
    testImplementation(testFixtures(project(":lib")))

    testImplementation(platform(testLibs.spring.boot.dependencies))

    testImplementation(testLibs.spring.boot.starter.oauth2.client)
    testImplementation(testLibs.spring.boot.starter.oauth2.resource.server)
    testImplementation(testLibs.spring.boot.starter.webflux)
}
