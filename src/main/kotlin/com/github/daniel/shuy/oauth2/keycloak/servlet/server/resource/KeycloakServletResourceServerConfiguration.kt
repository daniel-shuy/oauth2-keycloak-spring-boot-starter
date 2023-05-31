package com.github.daniel.shuy.oauth2.keycloak.servlet.server.resource

import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthenticationConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OAuth2ResourceServerAutoConfiguration::class)
@ConditionalOnClass(BearerTokenAuthenticationToken::class)
internal class KeycloakServletResourceServerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
    ): KeycloakOAuth2ResourceServerConfigurer = DefaultKeycloakOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter,
    )
}
