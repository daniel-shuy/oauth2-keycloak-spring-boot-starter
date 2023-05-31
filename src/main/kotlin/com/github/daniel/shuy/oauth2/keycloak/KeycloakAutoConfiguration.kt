package com.github.daniel.shuy.oauth2.keycloak

import com.github.daniel.shuy.oauth2.keycloak.converter.DefaultKeycloakJwtAuthenticationConverter
import com.github.daniel.shuy.oauth2.keycloak.converter.DefaultKeycloakJwtAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.converter.DefaultKeycloakJwtGrantedAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthenticationConverter
import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtGrantedAuthoritiesConverter
import com.github.daniel.shuy.oauth2.keycloak.reactive.KeycloakReactiveConfiguration
import com.github.daniel.shuy.oauth2.keycloak.reactive.KeycloakSecurityMatcherProvider
import com.github.daniel.shuy.oauth2.keycloak.reactive.client.KeycloakReactiveOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.reactive.server.resource.KeycloakReactiveOAuth2ResourceServerConfigurer
import com.github.daniel.shuy.oauth2.keycloak.servlet.KeycloakRequestMatcherProvider
import com.github.daniel.shuy.oauth2.keycloak.servlet.KeycloakServletConfiguration
import com.github.daniel.shuy.oauth2.keycloak.servlet.client.KeycloakOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.servlet.server.resource.KeycloakOAuth2ResourceServerConfigurer
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * [Auto-configuration][EnableAutoConfiguration] to configure Spring Security with Keycloak.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeycloakProperties::class)
@ConditionalOnProperty(
    prefix = KeycloakProperties.CONFIGURATION_PROPERTIES_PREFIX,
    name = ["enabled"], // TODO: use KeycloakProperties::enabled.name when KCallable.name are treated as compile-time constants (https://youtrack.jetbrains.com/issue/KT-58506)
    matchIfMissing = true,
)
@Import(
    KeycloakServletConfiguration::class,
    KeycloakReactiveConfiguration::class,
)
class KeycloakAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun keycloakJwtAuthoritiesConverter(keycloakProperties: KeycloakProperties): KeycloakJwtAuthoritiesConverter =
        DefaultKeycloakJwtAuthoritiesConverter(keycloakProperties.clientId)

    @Bean
    @ConditionalOnMissingBean
    fun keycloakJwtGrantedAuthoritiesConverter(
        keycloakJwtAuthoritiesConverter: KeycloakJwtAuthoritiesConverter,
    ): KeycloakJwtGrantedAuthoritiesConverter =
        DefaultKeycloakJwtGrantedAuthoritiesConverter(keycloakJwtAuthoritiesConverter)

    @Bean
    @ConditionalOnMissingBean
    fun keycloakJwtAuthenticationConverter(
        keycloakJwtGrantedAuthoritiesConverter: KeycloakJwtGrantedAuthoritiesConverter,
    ): KeycloakJwtAuthenticationConverter =
        DefaultKeycloakJwtAuthenticationConverter(keycloakJwtGrantedAuthoritiesConverter)

    @Bean
    fun keycloakWebSecurityConfigurer(
        keycloakOAuth2ClientConfigurer: KeycloakOAuth2ClientConfigurer?,
        keycloakReactiveOAuth2ClientConfigurer: KeycloakReactiveOAuth2ClientConfigurer?,
        keycloakOAuth2ResourceServerConfigurer: KeycloakOAuth2ResourceServerConfigurer?,
        keycloakReactiveOAuth2ResourceServerConfigurer: KeycloakReactiveOAuth2ResourceServerConfigurer?,
        keycloakRequestMatcherProvider: KeycloakRequestMatcherProvider?,
        keycloakSecurityMatcherProvider: KeycloakSecurityMatcherProvider?,
    ) = KeycloakWebSecurityConfigurer(
        keycloakOAuth2ClientConfigurer,
        keycloakReactiveOAuth2ClientConfigurer,
        keycloakOAuth2ResourceServerConfigurer,
        keycloakReactiveOAuth2ResourceServerConfigurer,
        keycloakRequestMatcherProvider,
        keycloakSecurityMatcherProvider,
    )
}
