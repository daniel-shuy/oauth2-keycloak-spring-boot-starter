plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
}

dependencies {
    testImplementation(testFixtures(project(":lib")))

    testImplementation(platform(testLibs.spring.boot.dependencies))

    testImplementation(testLibs.spring.boot.starter.oauth2.client)
    testImplementation(testLibs.spring.boot.starter.web)

    testImplementation(testLibs.alkemy.spring)
}
