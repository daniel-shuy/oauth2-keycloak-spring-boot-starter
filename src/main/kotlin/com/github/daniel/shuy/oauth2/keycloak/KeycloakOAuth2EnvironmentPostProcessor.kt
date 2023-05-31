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
object KeycloakOAuth2EnvironmentPostProcessor : EnvironmentPostProcessor {
    private const val SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_NAME = "keycloak"
    private const val SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME = "keycloak"
    private const val OAUTH2_AUTHORIZATION_GRANT_TYPE = "authorization_code"
    private const val OAUTH2_SCOPE = "openid"

    private const val PROPERTY_SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_ISSUER_URI =
        "spring.security.oauth2.resourceserver.jwt.issuer-uri"
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_ISSUER_URI =
        "spring.security.oauth2.client.provider.$SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_NAME.issuer-uri"
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER =
        "spring.security.oauth2.client.registration.$SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME.provider"
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_CLIENT_ID =
        "spring.security.oauth2.client.registration.$SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME.client-id"
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_CLIENT_SECRET =
        "spring.security.oauth2.client.registration.$SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME.client-secret"
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_AUTHORIZATION_GRANT_TYPE =
        "spring.security.oauth2.client.registration.$SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME.authorization-grant-type"
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_SCOPE =
        "spring.security.oauth2.client.registration.$SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME.scope"

    private val PROPERTY_ENABLED =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::enabled.name)}"
    private val PROPERTY_AUTH_SERVER_URL =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::authServerUrl.name)}"
    private val PROPERTY_REALM =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::realm.name)}"
    private val PROPERTY_CLIENT_ID =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::clientId.name)}"
    private val PROPERTY_CLIENT_SECRET =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::clientSecret.name)}"
    private val PROPERTY_BEARER_ONLY =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(KeycloakProperties::bearerOnly.name)}"

    @JvmStatic
    fun postProcessEnvironment(environment: ConfigurableEnvironment) {
        postProcessEnvironment(environment, null)
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication?) {
        if (environment.getProperty<Boolean>(PROPERTY_ENABLED) == false) {
            return
        }

        val propertySources = environment.propertySources

        val authServerUrl = environment.getRequiredProperty(PROPERTY_AUTH_SERVER_URL)
        val realm = environment.getRequiredProperty(PROPERTY_REALM)
        val clientId = environment.getRequiredProperty(PROPERTY_CLIENT_ID)
        val clientSecret = environment.getProperty(PROPERTY_CLIENT_SECRET)
        val bearerOnly = environment.getProperty<Boolean>(PROPERTY_BEARER_ONLY) ?: false

        val issuerUri = "$authServerUrl/realms/$realm"
        propertySources.addFirst(
            MapPropertySource(
                "keycloak",
                buildMap {
                    put(PROPERTY_SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_ISSUER_URI, issuerUri)
                    if (!bearerOnly) {
                        put(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_ISSUER_URI, issuerUri)
                        put(
                            PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER,
                            SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_NAME,
                        )
                        put(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_CLIENT_ID, clientId)
                        clientSecret?.let { put(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_CLIENT_SECRET, it) }
                        put(
                            PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_AUTHORIZATION_GRANT_TYPE,
                            OAUTH2_AUTHORIZATION_GRANT_TYPE,
                        )
                        put(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_SCOPE, OAUTH2_SCOPE)
                    }
                },
            ),
        )
    }
}
