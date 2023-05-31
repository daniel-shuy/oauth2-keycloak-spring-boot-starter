package com.github.daniel.shuy.oauth2.keycloak.reactive.client

import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthoritiesConverter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono

/**
 * Converts [OidcUserRequest] and [OidcUser] for Keycloak to Spring Security granted authorities.
 */
interface KeycloakReactiveOidcUserGrantedAuthoritiesConverter {
    fun toGrantedAuthorities(userRequest: OidcUserRequest, oidcUser: OidcUser): Mono<Collection<GrantedAuthority>>
}

/**
 * Converts [OidcUserRequest] and [OidcUser] for Keycloak to Spring Security granted authorities using the given
 * [ReactiveJwtDecoder] and [KeycloakJwtAuthoritiesConverter].
 */
open class DefaultKeycloakReactiveOidcUserGrantedAuthoritiesConverter(
    protected val jwtDecoder: ReactiveJwtDecoder,
    protected val keycloakJwtAuthoritiesConverter: KeycloakJwtAuthoritiesConverter,
) : KeycloakReactiveOidcUserGrantedAuthoritiesConverter {
    override fun toGrantedAuthorities(
        userRequest: OidcUserRequest,
        oidcUser: OidcUser,
    ) = jwtDecoder.decode(userRequest.accessToken.tokenValue)
        .mapNotNull(keycloakJwtAuthoritiesConverter::convert)
        .mapNotNull<Collection<GrantedAuthority>> { authorities ->
            authorities?.map { OidcUserAuthority(it, oidcUser.idToken, oidcUser.userInfo) }
        }
}
