package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.KeycloakWebSecurityConfigurer
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ContextConfiguration
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
class ServletSpec(
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
            fun keycloakClientFilterChain(
                http: HttpSecurity,
                keycloakWebSecurityConfigurer: KeycloakWebSecurityConfigurer,
            ): SecurityFilterChain {
                keycloakWebSecurityConfigurer.configureOAuth2Client(http)
                configureWebSecurity(http)
                return http.build()
            }

            @Bean
            fun keycloakResourceServerFilterChain(
                http: HttpSecurity,
                keycloakWebSecurityConfigurer: KeycloakWebSecurityConfigurer,
            ): SecurityFilterChain {
                keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http)
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

            response.statusCode.shouldBeEqual(HttpStatus.OK)
            response.body.shouldBeEqual(TestController.RESPONSE_BODY_HELLO_WORLD)
        }

        "Accessing protected resource with invalid bearer token should return HTTP 401 (Unauthorized)" {
            val requestEntity =
                HttpEntity<Unit>(
                    HttpHeaders().apply {
                        add(HttpHeaders.AUTHORIZATION, "INVALID_TOKEN")
                    },
                )
            val response =
                restTemplate.exchange<String>(
                    TestController.REQUEST_MAPPING_PATH_HELLO_WORLD,
                    HttpMethod.GET,
                    requestEntity,
                )

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
        }

        "Accessing protected resource without bearer token or session should redirect to Keycloak login page" {
            alkemyContext
                .get(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)
                .shouldRedirectToKeycloakLogin()
        }

        "XHR request to protected resource without bearer token or session should return HTTP 401 (Unauthorized)" {
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

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
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
