package com.github.daniel.shuy.oauth2.keycloak

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.context.properties.bind.DataObjectPropertyName
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.getProperty

/**
 * Configures [OAuth2ResourceServerProperties] and [OAuth2ClientProperties] for Keycloak using [KeycloakProperties].
 */
public object KeycloakOAuth2EnvironmentPostProcessor : EnvironmentPostProcessor {
    private const val OAUTH2_AUTHORIZATION_GRANT_TYPE = "authorization_code"
    private const val OAUTH2_SCOPE = "openid"

    private const val PROPERTY_SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_ISSUER_URI =
        "spring.security.oauth2.resourceserver.jwt.issuer-uri"

    private val PROPERTY_ENABLED =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::enabled.name)}"
    private val PROPERTY_AUTH_SERVER_URL =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${
            DataObjectPropertyName.toDashedForm(
                KeycloakProperties::authServerUrl.name,
            )
        }"
    private val PROPERTY_REALM =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::realm.name)}"
    private val PROPERTY_CLIENT_ID =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::clientId.name)}"
    private val PROPERTY_CLIENT_SECRET =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${
            DataObjectPropertyName.toDashedForm(
                KeycloakProperties::clientSecret.name,
            )
        }"
    private val PROPERTY_BEARER_ONLY =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::bearerOnly.name)}"
    private val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_NAME =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${
            DataObjectPropertyName.toDashedForm(
                KeycloakProperties::springSecurityOauth2ClientProviderName.name,
            )
        }"
    private val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${
            DataObjectPropertyName.toDashedForm(
                KeycloakProperties::springSecurityOauth2ClientRegistrationName.name,
            )
        }"

    @JvmStatic
    public fun postProcessEnvironment(environment: ConfigurableEnvironment) {
        postProcessEnvironment(environment, null)
    }

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication?,
    ) {
        if (environment.getProperty<Boolean>(PROPERTY_ENABLED) == false) {
            return
        }

        val propertySources = environment.propertySources

        val authServerUrl = environment.getRequiredProperty(PROPERTY_AUTH_SERVER_URL).appendIfMissing('/')
        val realm = environment.getRequiredProperty(PROPERTY_REALM)
        val clientId = environment.getRequiredProperty(PROPERTY_CLIENT_ID)
        val clientSecret = environment.getProperty(PROPERTY_CLIENT_SECRET)
        val bearerOnly = environment.getProperty<Boolean>(PROPERTY_BEARER_ONLY) ?: false
        val providerName = environment.getProperty(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_NAME)
        val registrationName = environment.getProperty(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME)

        val issuerUri = "$authServerUrl/realms/$realm"

        val issuerUriPropertyName = "spring.security.oauth2.client.provider.$providerName.issuer-uri"
        val providerPropertyName = "spring.security.oauth2.client.registration.$registrationName.provider"
        val clientIdPropertyName = "spring.security.oauth2.client.registration.$registrationName.client-id"
        val clientSecretPropertyName = "spring.security.oauth2.client.registration.$registrationName.client-secret"
        val authorizationGrantTypePropertyName =
            "spring.security.oauth2.client.registration.$registrationName.authorization-grant-type"
        val scopePropertyName = "spring.security.oauth2.client.registration.$registrationName.scope"

        propertySources.addFirst(
            MapPropertySource(
                "keycloak",
                buildMap {
                    put(PROPERTY_SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_ISSUER_URI, issuerUri)
                    if (!bearerOnly) {
                        put(issuerUriPropertyName, issuerUri)
                        put(
                            providerPropertyName,
                            providerName,
                        )
                        put(clientIdPropertyName, clientId)
                        clientSecret?.let { put(clientSecretPropertyName, it) }
                        put(
                            authorizationGrantTypePropertyName,
                            OAUTH2_AUTHORIZATION_GRANT_TYPE,
                        )
                        put(scopePropertyName, OAUTH2_SCOPE)
                    }
                },
            ),
        )
    }

    private fun String.appendIfMissing(char: Char): String = if (this.endsWith(char)) this else "$this$char"
}
