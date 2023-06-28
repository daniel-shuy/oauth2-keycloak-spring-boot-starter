package com.github.daniel.shuy.oauth2.keycloak.server.resource

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter

/**
 * Converts Keycloak bearer token to [AbstractAuthenticationToken].
 */
public interface KeycloakJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken>

/**
 * Converts Keycloak bearer token to [AbstractAuthenticationToken] using the given
 * [KeycloakJwtGrantedAuthoritiesConverter].
 */
public open class DefaultKeycloakJwtAuthenticationConverter(
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
