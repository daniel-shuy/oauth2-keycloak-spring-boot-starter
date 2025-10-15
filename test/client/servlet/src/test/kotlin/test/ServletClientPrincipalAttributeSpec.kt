package test

import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakHttpSecurityCustomizer
import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.test.context.ContextConfiguration
import test.KeycloakUtils.keycloakLogin
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
        "keycloak.principal-attribute=${StandardClaimNames.PREFERRED_USERNAME}",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ContextConfiguration(initializers = [TestcontainersKeycloakInitializer::class])
class ServletClientPrincipalAttributeSpec(
    playwrightConfigurationProperties: PlaywrightConfigurationProperties,
    @LocalServerPort serverPort: Number,
) : StringSpec() {
    val playwrightContext = configurePlaywright(playwrightConfigurationProperties, serverPort)

    @TestConfiguration
    class Configuration {
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
        "Principal name should be resolved from configured principal attribute in token claims" {
            val page = getPage(playwrightContext)
            page.navigate(TestController.REQUEST_MAPPING_PATH_PRINCIPAL_NAME)
            page.keycloakLogin()
            page.locator("body").assert {
                hasText(TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME)
            }
        }
    }
}
