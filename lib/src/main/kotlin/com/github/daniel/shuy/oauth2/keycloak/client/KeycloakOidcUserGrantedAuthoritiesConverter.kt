package com.github.daniel.shuy.oauth2.keycloak.client

import com.github.daniel.shuy.oauth2.keycloak.KeycloakJwtClaimsAuthoritiesConverter
import com.nimbusds.jwt.JWTParser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder

/**
 * Converts [OidcUserRequest] and [OidcUser] for Keycloak to Spring Security granted authorities.
 */
public interface KeycloakOidcUserGrantedAuthoritiesConverter {
    public fun toGrantedAuthorities(
        userRequest: OidcUserRequest,
        oidcUser: OidcUser,
    ): Collection<GrantedAuthority>?
}

/**
 * Converts [OidcUserRequest] and [OidcUser] for Keycloak to Spring Security granted authorities using the given
 * [JwtDecoder] and [KeycloakJwtClaimsAuthoritiesConverter].
 */
public open class DefaultKeycloakOidcUserGrantedAuthoritiesConverter(
    protected val keycloakJwtClaimsAuthoritiesConverter: KeycloakJwtClaimsAuthoritiesConverter,
) : KeycloakOidcUserGrantedAuthoritiesConverter {
    override fun toGrantedAuthorities(
        userRequest: OidcUserRequest,
        oidcUser: OidcUser,
    ): Collection<GrantedAuthority>? {
        val jwt = JWTParser.parse(userRequest.accessToken.tokenValue)
        return keycloakJwtClaimsAuthoritiesConverter
            .convert(jwt.jwtClaimsSet.claims)
            ?.map { OidcUserAuthority(it, oidcUser.idToken, oidcUser.userInfo) }
    }
}
