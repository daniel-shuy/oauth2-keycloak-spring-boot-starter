package com.github.daniel.shuy.oauth2.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX)
@Validated
public class KeycloakProperties {
    /**
     * Set to `false` to disable Spring Security integration with Keycloak.
     */
    public var enabled: Boolean = true

    /**
     * Base URL of the Keycloak server. All other Keycloak pages and REST service endpoints are derived from this.
     * It is usually of the form `https://host:port`.
     */
    public lateinit var authServerUrl: String

    /**
     * Name of the realm.
     */
    public lateinit var realm: String

    /**
     * Client-id of the application. Each application has a client-id that is used to identify the application.
     */
    public lateinit var clientId: String

    /**
     * Only for clients with `Confidential` access type. Specify the credentials of the application.
     */
    public var clientSecret: String? = null

    /**
     * If enabled, will not attempt to authenticate users, but only verify bearer tokens.
     */
    public var bearerOnly: Boolean = false

    /**
     * Token claim attribute to obtain as principal name. Defaults to token subject (`sub`).
     */
    public var principalAttribute: String = StandardClaimNames.SUB

    /**
     * Name of the Spring Security OAuth2 Client Provider to register.
     */
    public var springSecurityOauth2ClientProviderName: String = "keycloak"

    /**
     * Name of the Spring Security OAuth2 Client Registration to register.
     */
    public var springSecurityOauth2ClientRegistrationName: String = "keycloak"

    public companion object {
        public const val CONFIGURATION_PROPERTIES_PREFIX: String = "keycloak"
    }
}
