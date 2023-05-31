package com.github.daniel.shuy.oauth2.keycloak.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Converts Keycloak bearer token to Spring Security granted authorities.
 */
interface KeycloakJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>>

/**
 * Converts Keycloak bearer token to Spring Security granted authorities using the given
 * [KeycloakJwtAuthoritiesConverter].
 */
open class DefaultKeycloakJwtGrantedAuthoritiesConverter(
    protected val keycloakJwtAuthoritiesConverter: KeycloakJwtAuthoritiesConverter,
) : KeycloakJwtGrantedAuthoritiesConverter {
    override fun convert(jwt: Jwt) = keycloakJwtAuthoritiesConverter.convert(jwt)
        ?.map(::SimpleGrantedAuthority)
}
