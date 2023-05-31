package com.github.daniel.shuy.oauth2.keycloak.servlet.client

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthoritiesConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoder

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OAuth2ClientAutoConfiguration::class)
@ConditionalOnClass(ClientRegistration::class)
@ConditionalOnProperty(
    prefix = KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX,
    name = ["bearerOnly"], // TODO: use KeycloakProperties::bearerOnly.name when KCallable.name are treated as compile-time constants (https://youtrack.jetbrains.com/issue/KT-58506)
    havingValue = false.toString(),
    matchIfMissing = true,
)
internal class KeycloakServletClientConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakOidcUserGrantedAuthoritiesConverter(
        jwtDecoder: JwtDecoder,
        keycloakJwtAuthoritiesConverter: KeycloakJwtAuthoritiesConverter,
    ): KeycloakOidcUserGrantedAuthoritiesConverter = DefaultKeycloakOidcUserGrantedAuthoritiesConverter(
        jwtDecoder,
        keycloakJwtAuthoritiesConverter,
    )

    @Bean
    fun keycloakOidcUserService(
        keycloakOidcUserGrantedAuthoritiesConverter: KeycloakOidcUserGrantedAuthoritiesConverter,
    ) = KeycloakOidcUserService(keycloakOidcUserGrantedAuthoritiesConverter)

    @Bean
    @ConditionalOnMissingBean
    @Conditional(ClientsConfiguredCondition::class) // somehow @ConditionalOnBean(ClientRegistrationRepository::class) doesn't work
    fun keycloakOAuth2ClientConfigurer(
        clientRegistrationRepository: ClientRegistrationRepository,
        keycloakOidcUserService: KeycloakOidcUserService,
    ): KeycloakOAuth2ClientConfigurer = DefaultKeycloakOAuth2ClientConfigurer(
        clientRegistrationRepository,
        keycloakOidcUserService,
    )
}
