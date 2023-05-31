package com.github.daniel.shuy.oauth2.keycloak.reactive

import com.github.daniel.shuy.oauth2.keycloak.reactive.client.KeycloakReactiveClientConfiguration
import com.github.daniel.shuy.oauth2.keycloak.reactive.server.resource.KeycloakReactiveResourceServerConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import reactor.core.publisher.Flux

/**
 * Configuration to secure Reactive web application with Keycloak.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(
    Flux::class,
    EnableWebFluxSecurity::class,
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import(
    KeycloakReactiveClientConfiguration::class,
    KeycloakReactiveResourceServerConfiguration::class,
)
internal class KeycloakReactiveConfiguration
