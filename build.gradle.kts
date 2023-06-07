import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("org.springframework.boot") version Versions.springBoot apply false // don't build executable JAR
    id("io.spring.dependency-management") version Versions.dependencyManagementPlugin
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.spring") version Versions.kotlin
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlint
    id("org.jetbrains.dokka") version Versions.dokka
    id("net.researchgate.release") version Versions.releasePlugin
    `maven-publish`
    signing
}

group = "com.github.daniel-shuy"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot Starter for using Keycloak as the OAuth2 authorization server"

val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(Versions.java))
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
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
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

dependencyManagement {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-oauth2-client")
    compileOnly("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")

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
