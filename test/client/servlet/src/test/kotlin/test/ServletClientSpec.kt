package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakHttpSecurityCustomizer
import io.alkemy.assertions.shouldHaveText
import io.alkemy.spring.AlkemyProperties
import io.alkemy.spring.Extensions.alkemyContext
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.equals.shouldBeEqual
import org.keycloak.admin.client.Keycloak
import org.keycloak.util.TokenUtil
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.test.context.ContextConfiguration
import test.Extensions.keycloakLogin
import test.Extensions.keycloakLogout
import test.Extensions.shouldRedirectToKeycloakLogin
import test.Extensions.toClient
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
    alkemyProperties: AlkemyProperties,
    keycloakClient: Keycloak,
    restTemplate: TestRestTemplate,
    @LocalServerPort serverPort: Number,
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

        @EnableWebSecurity
        class WebSecurityConfig {
            @Bean
            fun keycloakHttpSecurityCustomizer() =
                KeycloakHttpSecurityCustomizer { http ->
                    http.authorizeRequests { authorize ->
                        authorize
                            .anyRequest()
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

        "Accessing protected resource without session should redirect to Keycloak login page" {
            alkemyContext
                .get(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .shouldRedirectToKeycloakLogin()
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
