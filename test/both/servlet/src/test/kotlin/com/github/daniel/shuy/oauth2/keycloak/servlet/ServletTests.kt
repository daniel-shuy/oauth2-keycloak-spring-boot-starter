package com.github.daniel.shuy.oauth2.keycloak.servlet

import com.github.daniel.shuy.oauth2.keycloak.KeycloakOAuth2EnvironmentPostProcessor
import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.KeycloakWebSecurityConfigurer
import dasniko.testcontainers.keycloak.KeycloakContainer
import io.alkemy.AlkemyContext
import io.alkemy.assertions.shouldHaveText
import io.alkemy.extensions.click
import io.alkemy.extensions.fillForm
import io.alkemy.spring.AlkemyProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
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
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ContextConfiguration

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
@ContextConfiguration(initializers = [ServletTests.Initializer::class])
class ServletTests(
    alkemyContext: AlkemyContext,
    keycloakClient: Keycloak,
    restTemplate: TestRestTemplate,
) : StringSpec() {
    @TestConfiguration
    @Lazy // required to use @LocalServerPort in @TestConfiguration
    class Configuration {
        @LocalServerPort
        private lateinit var serverPort: Number

        @Bean
        @Primary
        fun customAlkemyConfig(alkemyProperties: AlkemyProperties) = alkemyProperties.copy(
            baseUrl = "http://localhost:$serverPort",
        ).toAlkemyConfig()

        @Bean
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = Keycloak.getInstance(
            keycloakProperties.authServerUrl,
            keycloakProperties.realm,
            KEYCLOAK_USERNAME,
            KEYCLOAK_PASSWORD,
            keycloakProperties.clientId,
        )

        @EnableWebSecurity
        class WebSecurityConfig {
            @Bean
            fun keycloakResourceServerFilterChain(
                http: HttpSecurity,
                keycloakWebSecurityConfigurer: KeycloakWebSecurityConfigurer,
            ): SecurityFilterChain {
                keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http)
                configureWebSecurity(http)
                return http.build()
            }

            @Bean
            fun keycloakClientFilterChain(
                http: HttpSecurity,
                keycloakWebSecurityConfigurer: KeycloakWebSecurityConfigurer,
            ): SecurityFilterChain {
                keycloakWebSecurityConfigurer.configureOAuth2Client(http)
                configureWebSecurity(http)
                return http.build()
            }

            private fun configureWebSecurity(http: HttpSecurity) {
                http.authorizeRequests { authorize ->
                    authorize
                        .anyRequest()
                        .authenticated()
                }
            }
        }
    }

    init {
        "Accessing protected resource without bearer token or session should redirect to Keycloak login page" {
            val response = restTemplate.getForEntity<String>("/hello-world")

            response.statusCode.shouldBeEqual(HttpStatus.FOUND)
        }

        "XHR request to protected resource without bearer token or session should return HTTP 401 (Unauthorized)" {
            val requestEntity = HttpEntity<Unit>(
                HttpHeaders().apply {
                    add("X-Requested-With", "XMLHttpRequest")
                },
            )
            val response = restTemplate.exchange<String>("/hello-world", HttpMethod.GET, requestEntity)

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
        }

        "Accessing protected resource with invalid bearer token should return HTTP 401 (Unauthorized)" {
            val requestEntity = HttpEntity<Unit>(
                HttpHeaders().apply {
                    add(HttpHeaders.AUTHORIZATION, "INVALID_TOKEN")
                },
            )
            val response = restTemplate.exchange<String>("/hello-world", HttpMethod.GET, requestEntity)

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
        }

        "Protected resource should be accessible with valid bearer token" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            val requestEntity = HttpEntity<Unit>(
                HttpHeaders().apply {
                    add(HttpHeaders.AUTHORIZATION, bearerToken)
                },
            )
            val response = restTemplate.exchange<String>("/hello-world", HttpMethod.GET, requestEntity)

            response.statusCode.shouldBeEqual(HttpStatus.OK)
            response.body.shouldBeEqual("Hello, World!")
        }

        "Protected resource should be accessible after logging in" {
            alkemyContext.get("/hello-world")
                .fillForm(
                    "username" to KEYCLOAK_USERNAME,
                    "password" to KEYCLOAK_PASSWORD,
                )
                .click("input[type='submit']")
                .shouldHaveText("Hello, World!")
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
