package com.github.daniel.shuy.oauth2.keycloak.reactive.server.resource

import org.springframework.security.config.web.server.ServerHttpSecurity

/**
 * Configure filter as an OAuth2 resource server for Keycloak.
 */
interface KeycloakReactiveOAuth2ResourceServerConfigurer {
    fun configureOAuth2ResourceServer(http: ServerHttpSecurity)
}

/**
 * Configure filter as an OAuth2 resource server for Keycloak using `spring-security-oauth2-resource-server`.
 */
open class DefaultKeycloakReactiveOAuth2ResourceServerConfigurer(
    protected val keycloakJwtAuthenticationConverter: KeycloakReactiveJwtAuthenticationConverter,
) : KeycloakReactiveOAuth2ResourceServerConfigurer {
    override fun configureOAuth2ResourceServer(http: ServerHttpSecurity) {
        http.oauth2ResourceServer(::oauth2ResourceServer)
    }

    protected open fun oauth2ResourceServer(oauth2ResourceServer: ServerHttpSecurity.OAuth2ResourceServerSpec) {
        oauth2ResourceServer.jwt(::jwt)
    }

    protected open fun jwt(jwt: ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec) {
        jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
    }
}
