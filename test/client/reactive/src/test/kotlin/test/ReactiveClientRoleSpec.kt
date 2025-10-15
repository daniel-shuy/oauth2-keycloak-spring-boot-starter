package test

import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakServerHttpSecurityCustomizer
import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.ContextConfiguration
import test.KeycloakUtils.keycloakLogin
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
class ReactiveClientRoleSpec(
    playwrightConfigurationProperties: PlaywrightConfigurationProperties,
    @LocalServerPort serverPort: Number,
) : StringSpec() {
    val playwrightContext = configurePlaywright(playwrightConfigurationProperties, serverPort)

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

    init {
        "Protected resource should be accessible with required role" {
            val page = getPage(playwrightContext)
            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_FOO)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestReactiveController.RESPONSE_BODY_FOO)
            }
        }

        "Protected resource should be accessible with required permission" {
            val page = getPage(playwrightContext)
            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_BAR)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestReactiveController.RESPONSE_BODY_BAR)
            }
        }

        "Protected resource should not be accessible without required role" {
            val page = getPage(playwrightContext)
            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_FAIL_1)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText("Access Denied")
            }
        }

        "Protected resource should not be accessible without required permission" {
            val page = getPage(playwrightContext)
            page.navigate(TestReactiveController.REQUEST_MAPPING_PATH_FAIL_2)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText("Access Denied")
            }
        }
    }
}
