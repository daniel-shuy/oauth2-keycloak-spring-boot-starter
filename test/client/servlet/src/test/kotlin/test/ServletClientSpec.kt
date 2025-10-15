package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakHttpSecurityCustomizer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.keycloak.admin.client.Keycloak
import org.keycloak.util.TokenUtil
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.test.context.ContextConfiguration
import test.KeycloakUtils.keycloakLogin
import test.KeycloakUtils.keycloakLogout
import test.KeycloakUtils.shouldRedirectToKeycloakLogin
import test.KeycloakUtils.toClient
import test.playwright.PlaywrightConfigurationProperties
import test.playwright.PlaywrightContext.Companion.getPage
import test.playwright.PlaywrightUtils.assert
import test.playwright.PlaywrightUtils.configurePlaywright
import test.servlet.TestController

@SpringBootTest(
    properties = [
        "keycloak.auth-server-url=<placeholder>",
        "keycloak.realm=${TestcontainersKeycloakInitializer.KEYCLOAK_REALM}",
        "keycloak.client-id=${TestcontainersKeycloakInitializer.KEYCLOAK_CLIENT_ID}",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ContextConfiguration(initializers = [TestcontainersKeycloakInitializer::class])
class ServletClientSpec(
    keycloakClient: Keycloak,
    playwrightConfigurationProperties: PlaywrightConfigurationProperties,
    restTemplate: TestRestTemplate,
    @LocalServerPort serverPort: Number,
) : StringSpec() {
    val playwrightContext = configurePlaywright(playwrightConfigurationProperties, serverPort)

    @TestConfiguration
    class Configuration {
        @Bean
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = keycloakProperties.toClient()

        @Bean
        fun restTemplateBuilder(): RestTemplateBuilder =
            RestTemplateBuilder()
                .redirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW)

        @TestConfiguration
        @EnableWebSecurity
        class WebSecurityConfig {
            @Bean
            fun keycloakHttpSecurityCustomizer() =
                KeycloakHttpSecurityCustomizer { http ->
                    http.authorizeHttpRequests { authorize ->
                        authorize
                            .anyRequest()
                            .authenticated()
                    }
                }
        }
    }

    init {
        "Protected resource should be accessible after logging in" {
            val page = getPage(playwrightContext)
            page.navigate(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestController.RESPONSE_BODY_HELLO_WORLD)
            }
        }

        "Accessing protected resource without session should redirect to Keycloak login page" {
            val page = getPage(playwrightContext)
            page.navigate(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.shouldRedirectToKeycloakLogin()
        }

        "Accessing protected resource with valid bearer token but without session should redirect to Keycloak login page" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            val requestEntity =
                HttpEntity<Unit>(
                    HttpHeaders().apply {
                        add(HttpHeaders.AUTHORIZATION, bearerToken)
                    },
                )
            val response =
                restTemplate.exchange<String>(
                    TestController.REQUEST_MAPPING_PATH_HELLO_WORLD,
                    HttpMethod.GET,
                    requestEntity,
                )

            response.statusCode.shouldBeEqual(HttpStatus.FOUND)
        }

        "XHR request to protected resource without session should redirect to Keycloak login page" {
            val requestEntity =
                HttpEntity<Unit>(
                    HttpHeaders().apply {
                        accept = listOf(MediaType.APPLICATION_JSON)
                        add("X-Requested-With", "XMLHttpRequest")
                    },
                )
            val response =
                restTemplate.exchange<String>(
                    TestController.REQUEST_MAPPING_PATH_HELLO_WORLD,
                    HttpMethod.GET,
                    requestEntity,
                )

            response.statusCode.shouldBeEqual(HttpStatus.FOUND)
        }

        "Logout should invalidate session" {
            val page = getPage(playwrightContext)

            page.navigate(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestController.RESPONSE_BODY_HELLO_WORLD)
            }

            page.keycloakLogout()

            page.navigate(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
            page.shouldRedirectToKeycloakLogin()
        }
    }
}
