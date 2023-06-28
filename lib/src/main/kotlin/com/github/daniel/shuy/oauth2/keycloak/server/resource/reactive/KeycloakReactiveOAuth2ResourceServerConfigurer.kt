package com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive

import org.springframework.security.config.web.server.ServerHttpSecurity

/**
 * Configure filter as an OAuth2 resource server for Keycloak.
 */
public interface KeycloakReactiveOAuth2ResourceServerConfigurer {
    public fun configureOAuth2ResourceServer(http: ServerHttpSecurity)
}

/**
 * Configure filter as an OAuth2 resource server for Keycloak using `spring-security-oauth2-resource-server`.
 */
public open class DefaultKeycloakReactiveOAuth2ResourceServerConfigurer(
    private val keycloakJwtAuthenticationConverter: KeycloakReactiveJwtAuthenticationConverter,
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
