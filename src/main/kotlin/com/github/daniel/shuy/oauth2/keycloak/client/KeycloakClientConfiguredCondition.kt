package com.github.daniel.shuy.oauth2.keycloak.client

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import org.springframework.boot.autoconfigure.condition.ConditionMessage
import org.springframework.boot.autoconfigure.condition.ConditionOutcome
import org.springframework.boot.autoconfigure.condition.SpringBootCondition
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.bind.DataObjectPropertyName
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

internal object KeycloakClientConfiguredCondition : SpringBootCondition() {
    private const val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION = "spring.security.oauth2.client.registration"

    private val PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME =
        "${KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX}.${DataObjectPropertyName.toDashedForm(
            KeycloakProperties::springSecurityOauth2ClientRegistrationName.name,
        )}"

    override fun getMatchOutcome(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata?,
    ): ConditionOutcome {
        val message = ConditionMessage.forCondition("Keycloak Client Configured Condition")

        val environment = context.environment
        val registrationName = environment.getProperty(PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_NAME)

        val property = "$PROPERTY_SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION.$registrationName"

        return Binder
            .get(environment)
            .bind(property, OAuth2ClientProperties.Registration::class.java)
            .map { registration ->
                ConditionOutcome.match(
                    message.found("registered Keycloak client").items(registration.clientId),
                )
            }.orElseGet { ConditionOutcome.noMatch(message.didNotFind("$property property").atAll()) }
    }
}
