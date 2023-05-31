package com.github.daniel.shuy.oauth2.keycloak.servlet.client

import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthoritiesConverter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder

/**
 * Converts [OidcUserRequest] and [OidcUser] for Keycloak to Spring Security granted authorities.
 */
interface KeycloakOidcUserGrantedAuthoritiesConverter {
    fun toGrantedAuthorities(userRequest: OidcUserRequest, oidcUser: OidcUser): Collection<GrantedAuthority>?
}

/**
 * Converts [OidcUserRequest] and [OidcUser] for Keycloak to Spring Security granted authorities using the given
 * [JwtDecoder] and [KeycloakJwtAuthoritiesConverter].
 */
open class DefaultKeycloakOidcUserGrantedAuthoritiesConverter(
    protected val jwtDecoder: JwtDecoder,
    protected val keycloakJwtAuthoritiesConverter: KeycloakJwtAuthoritiesConverter,
) : KeycloakOidcUserGrantedAuthoritiesConverter {
    override fun toGrantedAuthorities(
        userRequest: OidcUserRequest,
        oidcUser: OidcUser,
    ): Collection<GrantedAuthority>? {
        val jwt = jwtDecoder.decode(userRequest.accessToken.tokenValue)
        return keycloakJwtAuthoritiesConverter.convert(jwt)
            ?.map { OidcUserAuthority(it, oidcUser.idToken, oidcUser.userInfo) }
    }
}
