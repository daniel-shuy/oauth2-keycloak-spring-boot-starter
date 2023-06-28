package com.github.daniel.shuy.oauth2.keycloak

import com.github.daniel.shuy.oauth2.keycloak.client.KeycloakClientConfiguration
import com.github.daniel.shuy.oauth2.keycloak.client.reactive.KeycloakReactiveOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.client.servlet.KeycloakOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.matcher.reactive.KeycloakSecurityMatcherProvider
import com.github.daniel.shuy.oauth2.keycloak.matcher.servlet.KeycloakRequestMatcherProvider
import com.github.daniel.shuy.oauth2.keycloak.server.resource.KeycloakResourceServerConfiguration
import com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive.KeycloakReactiveOAuth2ResourceServerConfigurer
import com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet.KeycloakOAuth2ResourceServerConfigurer
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
    KeycloakClientConfiguration::class,
    KeycloakResourceServerConfiguration::class,
)
public class KeycloakAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public fun keycloakJwtClaimsAuthoritiesConverter(keycloakProperties: KeycloakProperties): KeycloakJwtClaimsAuthoritiesConverter =
        DefaultKeycloakJwtClaimsAuthoritiesConverter(keycloakProperties.clientId)

    @Bean
    public fun keycloakWebSecurityConfigurer(
        keycloakOAuth2ClientConfigurer: KeycloakOAuth2ClientConfigurer?,
        keycloakReactiveOAuth2ClientConfigurer: KeycloakReactiveOAuth2ClientConfigurer?,
        keycloakOAuth2ResourceServerConfigurer: KeycloakOAuth2ResourceServerConfigurer?,
        keycloakReactiveOAuth2ResourceServerConfigurer: KeycloakReactiveOAuth2ResourceServerConfigurer?,
        keycloakRequestMatcherProvider: KeycloakRequestMatcherProvider?,
        keycloakSecurityMatcherProvider: KeycloakSecurityMatcherProvider?,
    ): KeycloakWebSecurityConfigurer = KeycloakWebSecurityConfigurer(
        keycloakOAuth2ClientConfigurer,
        keycloakReactiveOAuth2ClientConfigurer,
        keycloakOAuth2ResourceServerConfigurer,
        keycloakReactiveOAuth2ResourceServerConfigurer,
        keycloakRequestMatcherProvider,
        keycloakSecurityMatcherProvider,
    )
}
