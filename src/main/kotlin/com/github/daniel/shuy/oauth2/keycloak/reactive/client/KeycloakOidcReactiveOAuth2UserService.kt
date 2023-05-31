package com.github.daniel.shuy.oauth2.keycloak.reactive.client

import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono

/**
 * Maps Spring Security granted authorities for Keycloak [OidcUser]s using the given
 * [KeycloakReactiveOidcUserGrantedAuthoritiesConverter].
 */
internal class KeycloakOidcReactiveOAuth2UserService(
    private val keycloakReactiveOidcUserGrantedAuthoritiesConverter: KeycloakReactiveOidcUserGrantedAuthoritiesConverter,
) : OidcReactiveOAuth2UserService() {
    override fun loadUser(userRequest: OidcUserRequest): Mono<OidcUser> =
        super.loadUser(userRequest)
            .flatMap { oidcUser ->
                keycloakReactiveOidcUserGrantedAuthoritiesConverter.toGrantedAuthorities(userRequest, oidcUser)
                    .map { mappedAuthorities ->
                        // TODO: refactor when https://github.com/spring-projects/spring-security/pull/12282 is merged and released
                        val providerDetails = userRequest.clientRegistration.providerDetails
                        val userNameAttributeName = providerDetails.userInfoEndpoint.userNameAttributeName
                        return@map if (StringUtils.hasText(userNameAttributeName)) {
                            DefaultOidcUser(
                                mappedAuthorities,
                                oidcUser.idToken,
                                oidcUser.userInfo,
                                userNameAttributeName,
                            )
                        } else {
                            DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo)
                        }
                    }
            }
}
