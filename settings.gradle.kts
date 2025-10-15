rootProject.name = "oauth2-keycloak-spring-boot-starter"

include(
    "lib",
    "test",
    "test:both:reactive",
    "test:both:servlet",
    "test:client:reactive",
    "test:client:servlet",
    "test:resource-server:reactive",
    "test:resource-server:servlet",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("testLibs") {
            from(files("gradle/test-libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
