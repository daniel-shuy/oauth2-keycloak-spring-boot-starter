package test

import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakServerHttpSecurityCustomizer
import io.alkemy.assertions.shouldHaveText
import io.alkemy.spring.AlkemyProperties
import io.alkemy.spring.Extensions.alkemyContext
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.ContextConfiguration
import test.Extensions.keycloakLogin
import test.Extensions.keycloakLogout
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
class ReactiveClientRoleSpec(
    alkemyProperties: AlkemyProperties,
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
        @TestConfiguration
        @EnableWebFluxSecurity
        class WebFluxSecurityConfig {
            @Bean
            fun keycloakServerHttpSecurityCustomizer() =
                KeycloakServerHttpSecurityCustomizer { http ->
                    http.authorizeExchange { exchanges ->
                        exchanges
                            .pathMatchers(TestReactiveController.REQUEST_MAPPING_PATH_FOO)
                            .hasRole(TestcontainersKeycloakInitializer.KEYCLOAK_REALM_ROLE)

                            .pathMatchers(TestReactiveController.REQUEST_MAPPING_PATH_BAR)
                            .hasAuthority(TestcontainersKeycloakInitializer.KEYCLOAK_CLIENT_ROLE)

                            .pathMatchers(TestReactiveController.REQUEST_MAPPING_PATH_FAIL_1)
                            .hasRole("non-existent-role")

                            .pathMatchers(TestReactiveController.REQUEST_MAPPING_PATH_FAIL_2)
                            .hasAuthority("non-existent-authority")

                            .anyExchange()
                            .denyAll()
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
        "Protected resource should be accessible with required role" {
            alkemyContext
                .get(TestReactiveController.REQUEST_MAPPING_PATH_FOO)
                .keycloakLogin()
                .shouldHaveText(TestReactiveController.RESPONSE_BODY_FOO)
        }

        "Protected resource should be accessible with required permission" {
            alkemyContext
                .get(TestReactiveController.REQUEST_MAPPING_PATH_BAR)
                .keycloakLogin()
                .shouldHaveText(TestReactiveController.RESPONSE_BODY_BAR)
        }

        "Protected resource should not be accessible without required role" {
            alkemyContext
                .get(TestReactiveController.REQUEST_MAPPING_PATH_FAIL_1)
                .keycloakLogin()
                .shouldHaveText("Access Denied")
        }

        "Protected resource should not be accessible without required permission" {
            alkemyContext
                .get(TestReactiveController.REQUEST_MAPPING_PATH_FAIL_2)
                .keycloakLogin()
                .shouldHaveText("Access Denied")
        }
    }
}
