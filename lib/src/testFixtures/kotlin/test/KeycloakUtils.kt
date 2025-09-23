package test

import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Selenide
import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import org.keycloak.admin.client.Keycloak
import org.openqa.selenium.By
import test.SelenideUtils.submit
import com.codeborne.selenide.Selenide.`$` as findElement

object KeycloakUtils {
    fun KeycloakProperties.toClient(): Keycloak =
        Keycloak.getInstance(
            authServerUrl,
            realm,
            TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME,
            TestcontainersKeycloakInitializer.KEYCLOAK_PASSWORD,
            clientId,
        )

    fun shouldRedirectToKeycloakLogin() =
        findElement("body")
            .shouldHave(text("Sign in to your account"))

    fun keycloakLogin() {
        findElement(By.name("username")).value = TestcontainersKeycloakInitializer.KEYCLOAK_USERNAME
        findElement(By.name("password")).value = TestcontainersKeycloakInitializer.KEYCLOAK_PASSWORD
        submit()
    }

    fun keycloakLogout(contextPath: String? = "") {
        Selenide.open("$contextPath/logout")
        if (findElement("form").exists()) {
            submit()
        }
    }
}
