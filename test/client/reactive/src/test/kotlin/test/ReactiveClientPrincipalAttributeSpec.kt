package test

import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Selenide.open
import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakServerHttpSecurityCustomizer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.test.context.ContextConfiguration
import test.KeycloakUtils.keycloakLogin
import test.reactive.TestReactiveController
import com.codeborne.selenide.Selenide.`$` as findElement

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
class ReactiveClientPrincipalAttributeSpec(
    @LocalServerPort serverPort: Number,
) : SelenideSpec(serverPort) {
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
                            .anyExchange()
                            .authenticated()
                    }
                }
        }
    }

    init {
        "Principal name should be resolved from configured principal attribute in token claims" {
            open(TestReactiveController.REQUEST_MAPPING_PATH_PRINCIPAL_NAME)
            keycloakLogin()
            findElement("body")
                .shouldHave(text(TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME))
        }
    }
}
