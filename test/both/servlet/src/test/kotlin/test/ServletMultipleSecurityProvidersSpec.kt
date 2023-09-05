package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.KeycloakWebSecurityConfigurer
import com.github.daniel.shuy.oauth2.keycloak.matcher.servlet.KeycloakRequestMatcherProvider
import io.alkemy.AlkemyContext
import io.alkemy.assertions.shouldHaveText
import io.alkemy.spring.AlkemyProperties
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
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.servlet.handler.HandlerMappingIntrospector
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
class ServletMultipleSecurityProvidersSpec(
    private val alkemyContext: AlkemyContext,
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
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = keycloakProperties.toClient()

        @EnableWebSecurity
        class WebSecurityConfig {
            @Bean
            fun keycloakRequestMatcherProvider(introspector: HandlerMappingIntrospector) =
                KeycloakRequestMatcherProvider {
                    MvcRequestMatcher(introspector, "${PrefixedController.REQUEST_MAPPING}/**")
                }

            @Bean
            fun someOtherSecurityProviderFilterChain(http: HttpSecurity): SecurityFilterChain = http
                .authorizeRequests() { authorize ->
                    authorize
                        .anyRequest()
                        .denyAll()
                }
                .build()

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

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        alkemyContext.keycloakLogout(PrefixedController.REQUEST_MAPPING)
    }

    init {
        "Keycloak protected resource should be accessible after logging in" {
            alkemyContext
                .get("${PrefixedController.REQUEST_MAPPING}${TestController.REQUEST_MAPPING_PATH_HELLO_WORLD}")
                .keycloakLogin()
                .shouldHaveText(TestController.RESPONSE_BODY_HELLO_WORLD)
        }

        "Keycloak protected resource should be accessible with bearer token" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            val requestEntity = HttpEntity<Unit>(
                HttpHeaders().apply {
                    add(HttpHeaders.AUTHORIZATION, bearerToken)
                },
            )
            val response = restTemplate.exchange<String>(
                "${PrefixedController.REQUEST_MAPPING}${TestController.REQUEST_MAPPING_PATH_HELLO_WORLD}",
                HttpMethod.GET,
                requestEntity,
            )

            response.statusCode.shouldBeEqual(HttpStatus.OK)
            response.body.shouldBeEqual(TestController.RESPONSE_BODY_HELLO_WORLD)
        }

        "Accessing Keycloak protected resource without bearer token or session should redirect to Keycloak login page" {
            alkemyContext
                .get("${PrefixedController.REQUEST_MAPPING}${TestController.REQUEST_MAPPING_PATH_HELLO_WORLD}")
                .shouldRedirectToKeycloakLogin()
        }

        "Other security provider protected resource should not be accessible without bearer token" {
            val response = restTemplate.getForEntity<String>(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
        }

        "Other security provider protected resource should not be accessible with Keycloak bearer token" {
            val accessToken = keycloakClient.tokenManager().accessTokenString
            val bearerToken = "${TokenUtil.TOKEN_TYPE_BEARER} $accessToken"
            val requestEntity = HttpEntity<Unit>(
                HttpHeaders().apply {
                    add(HttpHeaders.AUTHORIZATION, bearerToken)
                },
            )
            val response = restTemplate.exchange<String>(
                TestController.REQUEST_MAPPING_PATH_HELLO_WORLD,
                HttpMethod.GET,
                requestEntity,
            )

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
        }
    }
}
