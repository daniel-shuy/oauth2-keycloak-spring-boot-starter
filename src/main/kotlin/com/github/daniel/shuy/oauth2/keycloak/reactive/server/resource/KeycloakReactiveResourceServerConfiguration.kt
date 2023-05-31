package com.github.daniel.shuy.oauth2.keycloak.reactive.server.resource

import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthenticationConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(ReactiveOAuth2ResourceServerAutoConfiguration::class)
@ConditionalOnClass(BearerTokenAuthenticationToken::class)
internal class KeycloakReactiveResourceServerConfiguration {
    @Bean
    fun keycloakReactiveJwtAuthenticationConverter(
        keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
    ) = KeycloakReactiveJwtAuthenticationConverter(keycloakJwtAuthenticationConverter)

    @Bean
    @ConditionalOnMissingBean
    fun keycloakReactiveOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter: KeycloakReactiveJwtAuthenticationConverter,
    ): KeycloakReactiveOAuth2ResourceServerConfigurer = DefaultKeycloakReactiveOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter,
    )
}
