package test

import com.github.daniel.shuy.oauth2.keycloak.KeycloakOAuth2EnvironmentPostProcessor
import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import dasniko.testcontainers.keycloak.KeycloakContainer
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.RealmRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.RolesRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.boot.context.properties.bind.DataObjectPropertyName
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class TestcontainersKeycloakInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        const val KEYCLOAK_REALM = "realm"
        const val KEYCLOAK_CLIENT_ID = "client"
        const val KEYCLOAK_USERNAME = "username"
        const val KEYCLOAK_PASSWORD = "password"
        const val KEYCLOAK_REALM_ROLE = "role"
        const val KEYCLOAK_CLIENT_ROLE = "permission"

        private val PROPERTY_AUTH_SERVER_URL =
            "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${
                DataObjectPropertyName.toDashedForm(
                    KeycloakProperties::authServerUrl.name,
                )
            }"
    }

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        val keycloakContainer = KeycloakContainer()
        keycloakContainer.start()

        val keycloakAdminClient = keycloakContainer.keycloakAdminClient
        keycloakAdminClient.realms().create(
            RealmRepresentation().apply {
                realm = KEYCLOAK_REALM
                isEnabled = true
                requiredActions = emptyList() // required to prevent "Account is not fully set up" issue
                clients =
                    listOf(
                        ClientRepresentation().apply {
                            clientId = KEYCLOAK_CLIENT_ID
                            redirectUris = listOf("*")
                            isPublicClient = true
                            isDirectAccessGrantsEnabled = true
                        },
                    )
                roles =
                    RolesRepresentation().apply {
                        realm =
                            listOf(
                                RoleRepresentation().apply {
                                    name = KEYCLOAK_REALM_ROLE
                                },
                            )
                        client =
                            mapOf(
                                KEYCLOAK_CLIENT_ID to
                                    listOf(
                                        RoleRepresentation().apply {
                                            name = KEYCLOAK_CLIENT_ROLE
                                        },
                                    ),
                            )
                    }
                users =
                    listOf(
                        UserRepresentation().apply {
                            username = KEYCLOAK_USERNAME
                            isEnabled = true
                            credentials =
                                listOf(
                                    CredentialRepresentation().apply {
                                        type = CredentialRepresentation.PASSWORD
                                        value = KEYCLOAK_PASSWORD
                                    },
                                )
                            realmRoles =
                                listOf(
                                    KEYCLOAK_REALM_ROLE,
                                )
                            clientRoles =
                                mapOf(
                                    KEYCLOAK_CLIENT_ID to
                                        listOf(
                                            KEYCLOAK_CLIENT_ROLE,
                                        ),
                                )
                        },
                    )
            },
        )

        TestPropertyValues
            .of(
                mapOf(
                    PROPERTY_AUTH_SERVER_URL to keycloakContainer.authServerUrl,
                ),
            ).applyTo(configurableApplicationContext)
        KeycloakOAuth2EnvironmentPostProcessor.postProcessEnvironment(configurableApplicationContext.environment)
    }
}
