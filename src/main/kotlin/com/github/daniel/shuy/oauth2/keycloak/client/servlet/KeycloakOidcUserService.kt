package com.github.daniel.shuy.oauth2.keycloak.client.servlet

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.util.StringUtils

/**
 * Maps Spring Security authorities for Keycloak [OidcUser]s using the given
 * [KeycloakOidcUserGrantedAuthoritiesConverter].
 */
class KeycloakOidcUserService(
    private val keycloakOidcUserGrantedAuthoritiesConverter: KeycloakOidcUserGrantedAuthoritiesConverter,
) : OidcUserService() {
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val mappedAuthorities = keycloakOidcUserGrantedAuthoritiesConverter.toGrantedAuthorities(userRequest, oidcUser)

        // TODO: refactor when https://github.com/spring-projects/spring-security/pull/12282 is merged and released
        val providerDetails = userRequest.clientRegistration.providerDetails
        val userNameAttributeName = providerDetails.userInfoEndpoint.userNameAttributeName
        return if (StringUtils.hasText(userNameAttributeName)) {
            DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo, userNameAttributeName)
        } else {
            DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo)
        }
    }
}
