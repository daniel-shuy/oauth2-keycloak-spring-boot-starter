package test

import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Selenide.open
import com.github.daniel.shuy.oauth2.keycloak.customizer.KeycloakHttpSecurityCustomizer
import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.test.context.ContextConfiguration
import test.KeycloakUtils.keycloakLogin
import test.servlet.TestController
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
class ServletClientRoleSpec(
    @LocalServerPort serverPort: Number,
) : StringSpec() {
    override val extensions = listOf(SelenideExtension(serverPort))

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
                            .requestMatchers(TestController.REQUEST_MAPPING_PATH_FOO)
                            .hasRole(TestcontainersKeycloakInitializer.KEYCLOAK_REALM_ROLE)

                            .requestMatchers(TestController.REQUEST_MAPPING_PATH_BAR)
                            .hasAuthority(TestcontainersKeycloakInitializer.KEYCLOAK_CLIENT_ROLE)

                            .requestMatchers(TestController.REQUEST_MAPPING_PATH_FAIL_1)
                            .hasRole("non-existent-role")

                            .requestMatchers(TestController.REQUEST_MAPPING_PATH_FAIL_2)
                            .hasAuthority("non-existent-authority")

                            .anyRequest()
                            .denyAll()
                    }
                }
        }
    }

    init {
        "Protected resource should be accessible with required role" {
            open(TestController.REQUEST_MAPPING_PATH_FOO)
            keycloakLogin()
            findElement("body")
                .shouldHave(text(TestController.RESPONSE_BODY_FOO))
        }

        "Protected resource should be accessible with required permission" {
            open(TestController.REQUEST_MAPPING_PATH_BAR)
            keycloakLogin()
            findElement("body")
                .shouldHave(text(TestController.RESPONSE_BODY_BAR))
        }

        "Protected resource should not be accessible without required role" {
            open(TestController.REQUEST_MAPPING_PATH_FAIL_1)
            keycloakLogin()
            findElement("body")
                .shouldNotHave(text(TestController.RESPONSE_BODY_FAIL))
        }

        "Protected resource should not be accessible without required permission" {
            open(TestController.REQUEST_MAPPING_PATH_FAIL_2)
            keycloakLogin()
            findElement("body")
                .shouldNotHave(text(TestController.RESPONSE_BODY_FAIL))
        }
    }
}
