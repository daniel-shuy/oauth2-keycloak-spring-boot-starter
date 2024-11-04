package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.config.KeycloakReactiveWebSecurityConfigurerAdapter
import io.alkemy.assertions.shouldHaveText
import io.alkemy.spring.AlkemyProperties
import io.alkemy.spring.Extensions.alkemyContext
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.keycloak.admin.client.Keycloak
import org.keycloak.util.TokenUtil
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import test.Extensions.keycloakLogin
import test.Extensions.keycloakLogout
import test.Extensions.shouldRedirectToKeycloakLogin
import test.Extensions.toClient

@SpringBootTest(
    properties = [
        "keycloak.auth-server-url=<placeholder>",
        "keycloak.realm=${TestcontainersKeycloakInitializer.KEYCLOAK_REALM}",
        "keycloak.client-id=${TestcontainersKeycloakInitializer.KEYCLOAK_CLIENT_ID}",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ContextConfiguration(initializers = [TestcontainersKeycloakInitializer::class])
class ReactiveSpec(
    alkemyProperties: AlkemyProperties,
    keycloakClient: Keycloak,
    @LocalServerPort serverPort: Number,
    webClient: WebTestClient,
) : StringSpec() {
    val alkemyContext =
        alkemyContext(
            alkemyProperties.copy(
                baseUrl = "http://localhost:$serverPort",
            ),
        )

    @TestConfiguration
    class Configuration {
        @Bean
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = keycloakProperties.toClient()

        @EnableWebFluxSecurity
        class WebFluxSecurityConfig {
            @Bean
            fun keycloakReactiveWebSecurityConfigurerAdapter() =
                KeycloakReactiveWebSecurityConfigurerAdapter { http ->
                    http.authorizeExchange { exchanges ->
                        exchanges
                            .anyExchange()
                            .authenticated()
                    }
                }
        }
    }

    override suspend fun afterEach(
        testCase: TestCase,
        result: TestResult,
    ) {
        alkemyContext.keycloakLogout()
    }

    init {
        "Protected resource should be accessible after logging in" {
            alkemyContext
                .get(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .keycloakLogin()
                .shouldHaveText(TestController.RESPONSE_BODY_HELLO_WORLD)
        }

        "Protected resource should be accessible with valid bearer token" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            webClient
                .get()
                .uri(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<String>()
                .isEqualTo(TestController.RESPONSE_BODY_HELLO_WORLD)
        }

        "Accessing protected resource with invalid bearer token should return HTTP 401 (Unauthorized)" {
            webClient
                .get()
                .uri(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .header(HttpHeaders.AUTHORIZATION, "INVALID_TOKEN")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        "Accessing protected resource without bearer token or session should redirect to Keycloak login page" {
            alkemyContext
                .get(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .shouldRedirectToKeycloakLogin()
        }

        "XHR request to protected resource without bearer token or session should return HTTP 401 (Unauthorized)" {
            webClient
                .get()
                .uri(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Requested-With", "XMLHttpRequest")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        "Logout should invalidate session" {
            alkemyContext
                .get(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .keycloakLogin()
                .shouldHaveText(TestController.RESPONSE_BODY_HELLO_WORLD)

            alkemyContext.keycloakLogout()

            alkemyContext
                .get(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .shouldRedirectToKeycloakLogin()
        }
    }
}
