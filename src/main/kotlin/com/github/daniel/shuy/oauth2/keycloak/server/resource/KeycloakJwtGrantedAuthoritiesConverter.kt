package com.github.daniel.shuy.oauth2.keycloak.server.resource

import com.github.daniel.shuy.oauth2.keycloak.KeycloakJwtClaimsAuthoritiesConverter
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Converts Keycloak bearer token to Spring Security granted authorities.
 */
public interface KeycloakJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>>

/**
 * Converts Keycloak bearer token to Spring Security granted authorities using the given
 * [KeycloakJwtClaimsAuthoritiesConverter].
 */
public open class DefaultKeycloakJwtGrantedAuthoritiesConverter(
    protected val keycloakJwtClaimsAuthoritiesConverter: KeycloakJwtClaimsAuthoritiesConverter,
) : KeycloakJwtGrantedAuthoritiesConverter {
    override fun convert(jwt: Jwt): List<SimpleGrantedAuthority>? =
        keycloakJwtClaimsAuthoritiesConverter
            .convert(jwt.claims)
            ?.map(::SimpleGrantedAuthority)
}
