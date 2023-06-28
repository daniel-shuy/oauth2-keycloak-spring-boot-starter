package com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive

import com.github.daniel.shuy.oauth2.keycloak.KeycloakOAuth2EnvironmentPostProcessor
import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.KeycloakWebSecurityConfigurer
import dasniko.testcontainers.keycloak.KeycloakContainer
import io.kotest.core.spec.style.StringSpec
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.RolesRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.keycloak.util.TokenUtil
import org.springframework.boot.context.properties.bind.DataObjectPropertyName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

private const val KEYCLOAK_REALM = "realm"
private const val KEYCLOAK_CLIENT_ID = "client"
private const val KEYCLOAK_USERNAME = "username"
private const val KEYCLOAK_PASSWORD = "password"

@SpringBootTest(
    properties = [
        "keycloak.auth-server-url=<placeholder>",
        "keycloak.realm=$KEYCLOAK_REALM",
        "keycloak.client-id=$KEYCLOAK_CLIENT_ID",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ContextConfiguration(initializers = [ReactiveResourceServerTests.Initializer::class])
class ReactiveResourceServerTests(
    keycloakClient: Keycloak,
    webClient: WebTestClient,
) : StringSpec() {
    @TestConfiguration
    class Configuration {
        @Bean
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = Keycloak.getInstance(
            keycloakProperties.authServerUrl,
            keycloakProperties.realm,
            KEYCLOAK_USERNAME,
            KEYCLOAK_PASSWORD,
            keycloakProperties.clientId,
        )

        @EnableWebFluxSecurity
        class WebFluxSecurityConfig {
            @Bean
            fun keycloakResourceServerFilterChain(
                http: ServerHttpSecurity,
                keycloakWebSecurityConfigurer: KeycloakWebSecurityConfigurer,
            ): SecurityWebFilterChain {
                keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http)

                return http
                    .authorizeExchange { exchanges ->
                        exchanges
                            .anyExchange()
                            .authenticated()
                    }
                    .build()
            }
        }
    }

    init {
        "Accessing protected resource without bearer token should return HTTP 401 (Unauthorized)" {
            webClient.get()
                .uri("/hello-world")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        "Accessing protected resource with invalid bearer token should return HTTP 401 (Unauthorized)" {
            webClient.get()
                .uri("/hello-world")
                .header(HttpHeaders.AUTHORIZATION, "INVALID_TOKEN")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        "Protected resource should be accessible with valid bearer token" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            webClient.get()
                .uri("/hello-world")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<String>()
                .isEqualTo("Hello, World!")
        }
    }

    object Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        private val PROPERTY_AUTH_SERVER_URL =
            "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${
            DataObjectPropertyName.toDashedForm(
                KeycloakProperties::authServerUrl.name,
            )
            }"

        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            val keycloakContainer = KeycloakContainer()
                .withContextPath("") // TODO: remove when https://github.com/dasniko/testcontainers-keycloak/pull/108 is merged and released
            keycloakContainer.start()

            val keycloakAdminClient = keycloakContainer.keycloakAdminClient
            keycloakAdminClient.realms().create(
                RealmRepresentation().apply {
                    realm = KEYCLOAK_REALM
                    isEnabled = true
                    clients = listOf(
                        ClientRepresentation().apply {
                            clientId = KEYCLOAK_CLIENT_ID
                            redirectUris = listOf("*")
                            isPublicClient = true
                            isDirectAccessGrantsEnabled = true
                        },
                    )
                    roles = RolesRepresentation().apply {
                        client = mapOf(
                            KEYCLOAK_CLIENT_ID to listOf(
                                RoleRepresentation().apply {
                                    name = "permission"
                                },
                            ),
                        )
                        realm = listOf(
                            RoleRepresentation().apply {
                                name = "role"
                            },
                        )
                    }
                    users = listOf(
                        UserRepresentation().apply {
                            username = KEYCLOAK_USERNAME
                            isEnabled = true
                            credentials = listOf(
                                CredentialRepresentation().apply {
                                    type = CredentialRepresentation.PASSWORD
                                    value = KEYCLOAK_PASSWORD
                                },
                            )
                        },
                    )
                },
            )

            TestPropertyValues.of(
                mapOf(
                    PROPERTY_AUTH_SERVER_URL to keycloakContainer.authServerUrl,
                ),
            ).applyTo(configurableApplicationContext)
            KeycloakOAuth2EnvironmentPostProcessor.postProcessEnvironment(configurableApplicationContext.environment)
        }
    }
}
