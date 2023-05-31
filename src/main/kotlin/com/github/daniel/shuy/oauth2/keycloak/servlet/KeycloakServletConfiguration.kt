package com.github.daniel.shuy.oauth2.keycloak.servlet

import com.github.daniel.shuy.oauth2.keycloak.servlet.client.KeycloakServletClientConfiguration
import com.github.daniel.shuy.oauth2.keycloak.servlet.server.resource.KeycloakServletResourceServerConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

/**
 * Configuration to secure web application with Keycloak.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableWebSecurity::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(
    KeycloakServletClientConfiguration::class,
    KeycloakServletResourceServerConfiguration::class,
)
internal class KeycloakServletConfiguration
