package com.github.daniel.shuy.oauth2.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX)
@ConstructorBinding
@Validated
public data class KeycloakProperties(
    /**
     * Set to `false` to disable Spring Security integration with Keycloak.
     */
    val enabled: Boolean = true,

    /**
     * Base URL of the Keycloak server. All other Keycloak pages and REST service endpoints are derived from this.
     * It is usually of the form `https://host:port`.
     */
    val authServerUrl: String,

    /**
     * Name of the realm.
     */
    val realm: String,

    /**
     * Client-id of the application. Each application has a client-id that is used to identify the application.
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

    /**
     * Name of the Spring Security OAuth2 Client Provider to register.
     */
    val springSecurityOauth2ClientProviderName: String = "keycloak",

    /**
     * Name of the Spring Security OAuth2 Client Registration to register.
     */
    val springSecurityOauth2ClientRegistrationName: String = "keycloak",
) {
    public companion object {
        public const val CONFIGURATION_PROPERTIES_PREFIX: String = "keycloak"
    }
}
