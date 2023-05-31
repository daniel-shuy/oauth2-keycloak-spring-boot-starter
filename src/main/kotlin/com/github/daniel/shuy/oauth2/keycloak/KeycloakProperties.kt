package com.github.daniel.shuy.oauth2.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX)
@ConstructorBinding
@Validated
data class KeycloakProperties(
    /**
     * Set to `false` to disable Spring Security integration with Keycloak.
     */
    val enabled: Boolean = true,

    /**
     * The base URL of the Keycloak server. All other Keycloak pages and REST service endpoints are derived from this.
     * It is usually of the form `https://host:port`.
     */
    val authServerUrl: String,

    /**
     * Name of the realm.
     */
    val realm: String,

    /**
     * The client-id of the application. Each application has a client-id that is used to identify the application.
     */
    val clientId: String,

    /**
     * Only for clients with `Confidential` access type. Specify the credentials of the application.
     */
    val clientSecret: String? = null,

    /**
     * If enabled, will not attempt to authenticate users, but only verify bearer tokens.
     */
    val bearerOnly: Boolean = false,
) {
    companion object {
        const val CONFIGURATION_PROPERTIES_PREFIX = "keycloak"
    }
}
