package com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet

import com.github.daniel.shuy.oauth2.keycloak.server.resource.KeycloakJwtAuthenticationConverter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer

/**
 * Configure filter as an OAuth2 resource server for Keycloak.
 */
interface KeycloakOAuth2ResourceServerConfigurer {
    fun configureOAuth2ResourceServer(http: HttpSecurity)
}

/**
 * Configure filter as an OAuth2 resource server for Keycloak using `spring-security-oauth2-resource-server`.
 */
open class DefaultKeycloakOAuth2ResourceServerConfigurer(
    protected val keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
) : KeycloakOAuth2ResourceServerConfigurer {
    override fun configureOAuth2ResourceServer(http: HttpSecurity) {
        http.oauth2ResourceServer(::oauth2ResourceServer)
    }

    protected open fun oauth2ResourceServer(oauth2ResourceServer: OAuth2ResourceServerConfigurer<HttpSecurity>) {
        oauth2ResourceServer.jwt(::jwt)
    }

    protected open fun jwt(jwt: OAuth2ResourceServerConfigurer<*>.JwtConfigurer) {
        jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
    }
}
