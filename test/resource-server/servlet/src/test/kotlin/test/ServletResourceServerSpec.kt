package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.KeycloakWebSecurityConfigurer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.keycloak.admin.client.Keycloak
import org.keycloak.util.TokenUtil
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ContextConfiguration
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
class ServletResourceServerSpec(
    keycloakClient: Keycloak,
    restTemplate: TestRestTemplate,
) : StringSpec() {
    @TestConfiguration
    class Configuration {
        @Bean
        fun keycloakClient(keycloakProperties: KeycloakProperties): Keycloak = keycloakProperties.toClient()

        @EnableWebSecurity
        class WebSecurityConfig {
            @Bean
            fun keycloakResourceServerFilterChain(
                http: HttpSecurity,
                keycloakWebSecurityConfigurer: KeycloakWebSecurityConfigurer,
            ): SecurityFilterChain {
                keycloakWebSecurityConfigurer.configureOAuth2ResourceServer(http)

                return http
                    .authorizeRequests { authorize ->
                        authorize
                            .anyRequest()
                            .authenticated()
                    }.build()
            }
        }
    }

    init {
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
            response.body.shouldNotBeNull().shouldBeEqual(TestController.RESPONSE_BODY_HELLO_WORLD)
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

        "Accessing protected resource without bearer token should return HTTP 401 (Unauthorized)" {
            val response = restTemplate.getForEntity<String>(TestController.REQUEST_MAPPING_PATH_HELLO_WORLD)

            response.statusCode.shouldBeEqual(HttpStatus.UNAUTHORIZED)
        }
    }
}
