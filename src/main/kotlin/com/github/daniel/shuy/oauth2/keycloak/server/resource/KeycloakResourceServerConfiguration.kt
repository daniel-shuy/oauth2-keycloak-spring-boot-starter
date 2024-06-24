package com.github.daniel.shuy.oauth2.keycloak.server.resource

import com.github.daniel.shuy.oauth2.keycloak.KeycloakJwtClaimsAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive.KeycloakReactiveResourceServerConfiguration
import com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet.KeycloakServletResourceServerConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken

/**
 * Configuration to configure web application as an OAuth2 resource server for Keycloak.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(BearerTokenAuthenticationToken::class)
@Import(
    KeycloakServletResourceServerConfiguration::class,
    KeycloakReactiveResourceServerConfiguration::class,
)
internal class KeycloakResourceServerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakJwtGrantedAuthoritiesConverter(
        keycloakJwtClaimsAuthoritiesConverter: KeycloakJwtClaimsAuthoritiesConverter,
    ): KeycloakJwtGrantedAuthoritiesConverter = DefaultKeycloakJwtGrantedAuthoritiesConverter(keycloakJwtClaimsAuthoritiesConverter)

    @Bean
    @ConditionalOnMissingBean
    fun keycloakJwtAuthenticationConverter(
        keycloakJwtGrantedAuthoritiesConverter: KeycloakJwtGrantedAuthoritiesConverter,
    ): KeycloakJwtAuthenticationConverter = DefaultKeycloakJwtAuthenticationConverter(keycloakJwtGrantedAuthoritiesConverter)
}
