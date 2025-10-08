package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.keycloak.admin.client.Keycloak
import test.playwright.PlaywrightUtils.submit

object KeycloakUtils {
    fun KeycloakProperties.toClient(): Keycloak =
        Keycloak.getInstance(
            authServerUrl,
            realm,
            TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME,
            TestcontainersKeycloakInitializer.KEYCLOAK_PASSWORD,
            clientId,
        )

    fun Page.shouldRedirectToKeycloakLogin() =
        assertThat(locator("body"))
            .containsText("Sign in to your account")

    fun Page.keycloakLogin() {
        locator("[name='username']").fill(TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME)
        locator("[name='password']").fill(TestcontainersKeycloakInitializer.KEYCLOAK_PASSWORD)
        submit()
    }

    fun Page.keycloakLogout(contextPath: String? = "") {
        navigate("$contextPath/logout")
        if (locator("form").isVisible) {
            submit()
        }
    }
}
