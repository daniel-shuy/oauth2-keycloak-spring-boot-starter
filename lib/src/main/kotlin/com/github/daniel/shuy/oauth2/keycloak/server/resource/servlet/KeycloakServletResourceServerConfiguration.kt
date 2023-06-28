package com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet

import com.github.daniel.shuy.oauth2.keycloak.server.resource.KeycloakJwtAuthenticationConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OAuth2ResourceServerAutoConfiguration::class)
@ConditionalOnClass(EnableWebSecurity::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
internal class KeycloakServletResourceServerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
    ): KeycloakOAuth2ResourceServerConfigurer = DefaultKeycloakOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter,
    )
}
