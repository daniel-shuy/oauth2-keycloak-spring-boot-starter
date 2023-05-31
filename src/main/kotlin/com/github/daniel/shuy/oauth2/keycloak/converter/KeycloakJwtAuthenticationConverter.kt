package com.github.daniel.shuy.oauth2.keycloak.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter

/**
 * Converts Keycloak bearer token to [AbstractAuthenticationToken].
 */
interface KeycloakJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken>

/**
 * Converts Keycloak bearer token to [AbstractAuthenticationToken] using the given
 * [KeycloakJwtGrantedAuthoritiesConverter].
 */
open class DefaultKeycloakJwtAuthenticationConverter(
    keycloakJwtGrantedAuthoritiesConverter: KeycloakJwtGrantedAuthoritiesConverter,
) : JwtAuthenticationConverter(), KeycloakJwtAuthenticationConverter {
    init {
        setKeycloakJwtGrantedAuthoritiesConverter(keycloakJwtGrantedAuthoritiesConverter)
    }

    private fun setKeycloakJwtGrantedAuthoritiesConverter(
        keycloakJwtGrantedAuthoritiesConverter: KeycloakJwtGrantedAuthoritiesConverter,
    ) {
        setJwtGrantedAuthoritiesConverter(keycloakJwtGrantedAuthoritiesConverter)
    }
}
