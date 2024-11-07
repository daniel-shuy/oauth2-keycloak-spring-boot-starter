package com.github.daniel.shuy.oauth2.keycloak

import com.github.daniel.shuy.oauth2.keycloak.client.KeycloakClientConfiguration
import com.github.daniel.shuy.oauth2.keycloak.config.DefaultKeycloakReactiveWebSecurityConfigurerAdapter
import com.github.daniel.shuy.oauth2.keycloak.config.DefaultKeycloakWebSecurityConfigurerAdapter
import com.github.daniel.shuy.oauth2.keycloak.config.KeycloakReactiveWebSecurityConfigurerAdapter
import com.github.daniel.shuy.oauth2.keycloak.config.KeycloakWebSecurityConfigurerAdapter
import com.github.daniel.shuy.oauth2.keycloak.server.resource.KeycloakResourceServerConfiguration
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
    /*
        TODO: use KeycloakProperties::enabled.name when KCallable.name are treated as compile-time constants
        (https://youtrack.jetbrains.com/issue/KT-58506)
     */
    name = ["enabled"],
    matchIfMissing = true,
)
@Import(
    KeycloakClientConfiguration::class,
    KeycloakResourceServerConfiguration::class,
)
public class KeycloakAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public fun keycloakReactiveWebSecurityConfigurerAdapter(): KeycloakReactiveWebSecurityConfigurerAdapter =
        DefaultKeycloakReactiveWebSecurityConfigurerAdapter()

    @Bean
    @ConditionalOnMissingBean
    public fun keycloakWebSecurityConfigurerAdapter(): KeycloakWebSecurityConfigurerAdapter = DefaultKeycloakWebSecurityConfigurerAdapter()

    @Bean
    @ConditionalOnMissingBean
    public fun keycloakJwtClaimsAuthoritiesConverter(keycloakProperties: KeycloakProperties): KeycloakJwtClaimsAuthoritiesConverter =
        DefaultKeycloakJwtClaimsAuthoritiesConverter(keycloakProperties.clientId)
}
