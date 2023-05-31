import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val springBootVersion = "2.6.5" // lowest Spring Boot version that supports Gradle 8
    val kotlinVersion = "1.6.10" // Kotlin version that is supported by Spring Boot version

    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jetbrains.dokka") version "1.8.10"
    id("net.researchgate.release") version "3.0.2"
    `maven-publish`
    signing
}

group = "com.github.daniel-shuy"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot Starter for using Keycloak as the OAuth2 authorization server"

val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
    }
}

// don't build executable JAR
tasks {
    bootJar {
        enabled = false
    }
    jar {
        archiveClassifier.set("")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

val javadocJar = tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

tasks.named("afterReleaseBuild") {
    dependsOn("publish")
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components["java"])

            pom {
                url.set("https://github.com/daniel-shuy/oauth2-keycloak-spring-boot-starter")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("daniel-shuy")
                        name.set("Daniel Shuy")
                        email.set("daniel_shuy@hotmail.com")
                        url.set("https://github.com/daniel-shuy")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/daniel-shuy/oauth2-keycloak-spring-boot-starter.git")
                    developerConnection.set("scm:git:https://github.com/daniel-shuy/oauth2-keycloak-spring-boot-starter.git")
                    url.set("https://github.com/daniel-shuy/oauth2-keycloak-spring-boot-starter")
                    tag.set("HEAD")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (isReleaseVersion) releasesRepoUrl else snapshotsRepoUrl)

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/daniel-shuy/oauth2-keycloak-spring-boot-starter")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    setRequired(
        { // skip signing for snapshots
            isReleaseVersion && gradle.taskGraph.hasTask("publish")
        },
    )

    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["lib"])
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
