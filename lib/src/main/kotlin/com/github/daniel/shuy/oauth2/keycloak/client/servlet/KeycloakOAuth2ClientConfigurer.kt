package com.github.daniel.shuy.oauth2.keycloak.client.servlet

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

/**
 * Configure filter as an OAuth2 Client for Keycloak.
 */
public interface KeycloakOAuth2ClientConfigurer {
    public fun configureOAuth2Client(http: HttpSecurity)
}

/**
 * Configure filter as an OAuth2 Client for Keycloak using `spring-security-oauth2-client`.
 */
public open class DefaultKeycloakOAuth2ClientConfigurer(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val keycloakOidcUserService: KeycloakOidcUserService,
) : KeycloakOAuth2ClientConfigurer {
    override fun configureOAuth2Client(http: HttpSecurity) {
        http
            .csrf(::csrf)
            .oauth2Login(::oauth2Login)
            .logout { logout -> logout(logout, clientRegistrationRepository) }
    }

    /**
     * Since the filter is only used for unauthenticated requests or GET requests, it does not need CSRF protection.
     */
    protected open fun csrf(csrf: CsrfConfigurer<HttpSecurity>) {
        csrf.disable()
    }

    protected open fun oauth2Login(oauth2Login: OAuth2LoginConfigurer<HttpSecurity>) {
        oauth2Login.permitAll()
        oauth2Login.userInfoEndpoint(::userInfoEndpoint)
    }

    protected open fun userInfoEndpoint(userInfoEndpoint: OAuth2LoginConfigurer<*>.UserInfoEndpointConfig) {
        userInfoEndpoint.oidcUserService(keycloakOidcUserService)
    }

    protected open fun logout(
        logout: LogoutConfigurer<HttpSecurity>,
        clientRegistrationRepository: ClientRegistrationRepository,
    ) {
        logout.logoutSuccessHandler(getLogoutSuccessHandler(clientRegistrationRepository))
    }

    protected open fun getLogoutSuccessHandler(clientRegistrationRepository: ClientRegistrationRepository): LogoutSuccessHandler =
        OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)
}
