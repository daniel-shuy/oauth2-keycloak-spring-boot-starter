package com.github.daniel.shuy.oauth2.keycloak.client.reactive

import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler

/**
 * Configure filter as an OAuth2 Client for Keycloak.
 */
public interface KeycloakReactiveOAuth2ClientConfigurer {
    public fun configureOAuth2Client(http: ServerHttpSecurity)
}

/**
 * Configure filter as an OAuth2 Client for Keycloak using `spring-security-oauth2-client`.
 */
public open class DefaultKeycloakReactiveOAuth2ClientConfigurer(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : KeycloakReactiveOAuth2ClientConfigurer {
    override fun configureOAuth2Client(http: ServerHttpSecurity) {
        http
            .csrf(::csrf)
            .oauth2Login(::oauth2Login)
            .logout { logout -> logout(logout, clientRegistrationRepository) }
    }

    /**
     * Since the filter is only used for unauthenticated requests or GET requests, it does not need CSRF protection.
     */
    protected open fun csrf(csrf: ServerHttpSecurity.CsrfSpec) {
        csrf.disable()
    }

    protected open fun oauth2Login(oauth2Login: ServerHttpSecurity.OAuth2LoginSpec) {}

    protected open fun logout(
        logout: ServerHttpSecurity.LogoutSpec,
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
    ) {
        logout.logoutSuccessHandler(getLogoutSuccessHandler(clientRegistrationRepository))
    }

    protected open fun getLogoutSuccessHandler(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
    ): ServerLogoutSuccessHandler = OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository)
}
