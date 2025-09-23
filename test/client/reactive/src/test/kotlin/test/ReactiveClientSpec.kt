package test

import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Selenide.open
import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakServerHttpSecurityCustomizer
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
import test.KeycloakUtils.keycloakLogin
import test.KeycloakUtils.keycloakLogout
import test.KeycloakUtils.shouldRedirectToKeycloakLogin
import test.KeycloakUtils.toClient
import test.reactive.TestReactiveController
import com.codeborne.selenide.Selenide.`$` as findElement

@SpringBootTest(
    properties = [
        "keycloak.auth-server-url=<placeholder>",
        "keycloak.realm=${TestcontainersKeycloakInitializer.KEYCLOAK_REALM}",
        "keycloak.client-id=${TestcontainersKeycloakInitializer.KEYCLOAK_CLIENT_ID}",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ContextConfiguration(initializers = [TestcontainersKeycloakInitializer::class])
class ReactiveClientSpec(
    keycloakClient: Keycloak,
    @LocalServerPort serverPort: Number,
    webClient: WebTestClient,
) : SelenideSpec(serverPort) {
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
            open(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            keycloakLogin()
            findElement("body")
                .shouldHave(text(TestReactiveController.RESPONSE_BODY_HELLO_WORLD))
        }

        "Accessing protected resource without session should redirect to Keycloak login page" {
            open(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            shouldRedirectToKeycloakLogin()
        }

        "Accessing protected resource with valid bearer token but without session should redirect to Keycloak login page" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            webClient
                .get()
                .uri(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .exchange()
                .expectStatus()
                .isFound
        }

        "XHR request to protected resource without session should redirect to Keycloak login page" {
            webClient
                .get()
                .uri(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Requested-With", "XMLHttpRequest")
                .exchange()
                .expectStatus()
                .isFound
        }

        "Logout should invalidate session" {
            open(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            keycloakLogin()
            findElement("body")
                .shouldHave(text(TestReactiveController.RESPONSE_BODY_HELLO_WORLD))

            keycloakLogout()

            open(TestReactiveController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            shouldRedirectToKeycloakLogin()
        }
    }
}
