package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakServerHttpSecurityCustomizer
import io.kotest.core.spec.style.StringSpec
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
import test.KeycloakUtils.keycloakLogin
import test.KeycloakUtils.keycloakLogout
import test.KeycloakUtils.shouldRedirectToKeycloakLogin
import test.KeycloakUtils.toClient
import test.playwright.PlaywrightConfigurationProperties
import test.playwright.PlaywrightContext.Companion.getPage
import test.playwright.PlaywrightUtils.assert
import test.playwright.PlaywrightUtils.configurePlaywright
import test.reactive.TestReactiveController

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
    keycloakClient: Keycloak,
    playwrightConfigurationProperties: PlaywrightConfigurationProperties,
    @LocalServerPort serverPort: Number,
    webClient: WebTestClient,
) : StringSpec() {
    val playwrightContext = configurePlaywright(playwrightConfigurationProperties, serverPort)

    @TestConfiguration
    class Configuration {
        @Bean
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = keycloakProperties.toClient()

        @TestConfiguration
        @EnableWebFluxSecurity
        class WebFluxSecurityConfig {
            @Bean
            fun keycloakServerHttpSecurityCustomizer() =
                KeycloakServerHttpSecurityCustomizer { http ->
                    http.authorizeExchange { exchanges ->
                        exchanges
                            .anyExchange()
                            .authenticated()
                    }
                }
        }
    }

    init {
        "Protected resource should be accessible after logging in" {
            val page = getPage(playwrightContext)
            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestReactiveController.RESPONSE_BODY_HELLO_WORLD)
            }
        }

        "Protected resource should be accessible with valid bearer token" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            webClient
                .get()
                .uri(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<String>()
                .isEqualTo(TestReactiveController.RESPONSE_BODY_HELLO_WORLD)
        }

        "Accessing protected resource with invalid bearer token should return HTTP 401 (Unauthorized)" {
            webClient
                .get()
                .uri(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .header(HttpHeaders.AUTHORIZATION, "INVALID_TOKEN")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        "Accessing protected resource without bearer token or session should redirect to Keycloak login page" {
            val page = getPage(playwrightContext)
            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.shouldRedirectToKeycloakLogin()
        }

        "XHR request to protected resource without bearer token or session should return HTTP 401 (Unauthorized)" {
            webClient
                .get()
                .uri(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Requested-With", "XMLHttpRequest")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        "Logout should invalidate session" {
            val page = getPage(playwrightContext)

            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestReactiveController.RESPONSE_BODY_HELLO_WORLD)
            }

            page.keycloakLogout()

            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.shouldRedirectToKeycloakLogin()
        }
    }
}
