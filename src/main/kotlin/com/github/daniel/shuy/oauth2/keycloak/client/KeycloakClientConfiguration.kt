package com.github.daniel.shuy.oauth2.keycloak.client

import com.github.daniel.shuy.oauth2.keycloak.KeycloakJwtClaimsAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import com.github.daniel.shuy.oauth2.keycloak.client.reactive.KeycloakReactiveClientConfiguration
import com.github.daniel.shuy.oauth2.keycloak.client.servlet.KeycloakServletClientConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistration

/**
 * Configuration to configure web application as an OAuth2 client for Keycloak.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ClientRegistration::class)
@ConditionalOnProperty(
    prefix = KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX,
    name = ["bearerOnly"], // TODO: use KeycloakProperties::bearerOnly.name when KCallable.name are treated as compile-time constants (https://youtrack.jetbrains.com/issue/KT-58506)
    havingValue = false.toString(),
    matchIfMissing = true,
)
@Import(
    KeycloakServletClientConfiguration::class,
    KeycloakReactiveClientConfiguration::class,
)
internal class KeycloakClientConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakOidcUserGrantedAuthoritiesConverter(
        keycloakJwtClaimsAuthoritiesConverter: KeycloakJwtClaimsAuthoritiesConverter,
    ): KeycloakOidcUserGrantedAuthoritiesConverter = DefaultKeycloakOidcUserGrantedAuthoritiesConverter(
        keycloakJwtClaimsAuthoritiesConverter,
    )
}
