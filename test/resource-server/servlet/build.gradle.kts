plugins {
    alias(testLibs.plugins.kotlin.jvm)
    alias(testLibs.plugins.kotlin.spring)
    alias(testLibs.plugins.ktlint)
}

dependencies {
    testImplementation(testFixtures(project(":lib")))

    testImplementation(platform(testLibs.spring.boot.dependencies))

    testImplementation(testLibs.spring.boot.starter.oauth2.resource.server)
    testImplementation(testLibs.spring.boot.starter.web)
}
