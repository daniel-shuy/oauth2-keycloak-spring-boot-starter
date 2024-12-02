package com.github.daniel.shuy.oauth2.keycloak.server.resource

import com.github.daniel.shuy.oauth2.keycloak.KeycloakProperties
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**
 * Converts Keycloak bearer token to [AbstractAuthenticationToken].
 */
public interface KeycloakJwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken>

/**
 * Converts Keycloak bearer token to [AbstractAuthenticationToken] using the given
 * [KeycloakJwtGrantedAuthoritiesConverter].
 */
public open class DefaultKeycloakJwtAuthenticationConverter(
    private val keycloakJwtGrantedAuthoritiesConverter: KeycloakJwtGrantedAuthoritiesConverter,
    private val keycloakProperties: KeycloakProperties,
) : KeycloakJwtAuthenticationConverter {
    override fun convert(jwt: Jwt): AbstractAuthenticationToken? {
        val authorities = keycloakJwtGrantedAuthoritiesConverter.convert(jwt)
        return JwtAuthenticationToken(jwt, authorities, getPrincipalName(jwt))
    }

    protected open fun getPrincipalName(jwt: Jwt): String = jwt.getClaimAsString(keycloakProperties.principalAttribute)
}
