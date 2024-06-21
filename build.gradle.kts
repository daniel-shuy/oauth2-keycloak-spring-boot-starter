import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka)
    alias(libs.plugins.release)
    `maven-publish`
    signing
}

group = "com.github.daniel-shuy"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot Starter for using Keycloak as the OAuth2 authorization server"

val isReleaseVersion = !version.toString().endsWith("-SNAPSHOT")

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
    }
    coreLibrariesVersion = libs.versions.kotlinLib.get()
    explicitApi()
}

java {
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    withJavadocJar()
    withSourcesJar()
}

val javadocJar =
    tasks.named<Jar>("javadocJar") {
        from(tasks.named("dokkaJavadoc"))
    }

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version = libs.versions.ktlint
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
        {
            // skip signing for snapshots
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
    compileOnly(libs.spring.boot.starter.oauth2.client)
    compileOnly(libs.spring.boot.starter.oauth2.resource.server)
    compileOnly(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.starter.webflux)

    implementation(libs.spring.boot.starter)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
