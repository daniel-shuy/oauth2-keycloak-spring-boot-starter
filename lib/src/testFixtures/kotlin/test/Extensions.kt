package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import io.alkemy.AlkemyContext
import io.alkemy.assertions.shouldHaveText
import io.alkemy.extensions.click
import io.alkemy.extensions.fillForm
import io.alkemy.extensions.findElements
import org.keycloak.admin.client.Keycloak
import org.openqa.selenium.WebDriver

object Extensions {
    fun KeycloakProperties.toClient(): Keycloak =
        Keycloak.getInstance(
            authServerUrl,
            realm,
            TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME,
            TestcontainersKeycloakInitializer.KEYCLOAK_PASSWORD,
            clientId,
        )

    fun WebDriver.shouldRedirectToKeycloakLogin() = shouldHaveText("Sign in to your account")

    fun WebDriver.keycloakLogin() =
        this
            .fillForm(
                "username" to TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME,
                "password" to TestcontainersKeycloakInitializer.KEYCLOAK_PASSWORD,
            ).click("input[type='submit']")

    fun AlkemyContext.keycloakLogout(contextPath: String? = "") =
        this
            .get("$contextPath/logout")
            .findElements("button[type='submit']")
            .firstOrNull()
            ?.click()
}
