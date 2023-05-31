package com.github.daniel.shuy.oauth2.keycloak

import com.github.daniel.shuy.oauth2.keycloak.reactive.KeycloakSecurityMatcherProvider
import com.github.daniel.shuy.oauth2.keycloak.reactive.client.KeycloakReactiveOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.reactive.matcher.AnyServerWebExchangeMatcher
import com.github.daniel.shuy.oauth2.keycloak.reactive.matcher.RedirectToInteractiveLoginServerWebExchangeMatcher
import com.github.daniel.shuy.oauth2.keycloak.reactive.server.resource.KeycloakReactiveOAuth2ResourceServerConfigurer
import com.github.daniel.shuy.oauth2.keycloak.servlet.KeycloakRequestMatcherProvider
import com.github.daniel.shuy.oauth2.keycloak.servlet.client.KeycloakOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.servlet.matcher.RedirectToInteractiveLoginRequestMatcher
import com.github.daniel.shuy.oauth2.keycloak.servlet.server.resource.KeycloakOAuth2ResourceServerConfigurer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.util.matcher.AndRequestMatcher
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher

/**
 * Utility class to configure web security for Keycloak.
 */
class KeycloakWebSecurityConfigurer(
    private val keycloakOAuth2ClientConfigurer: KeycloakOAuth2ClientConfigurer?,
    private val keycloakReactiveOAuth2ClientConfigurer: KeycloakReactiveOAuth2ClientConfigurer?,
    private val keycloakOAuth2ResourceServerConfigurer: KeycloakOAuth2ResourceServerConfigurer?,
    private val keycloakReactiveOAuth2ResourceServerConfigurer: KeycloakReactiveOAuth2ResourceServerConfigurer?,
    keycloakRequestMatcherProvider: KeycloakRequestMatcherProvider?,
    keycloakSecurityMatcherProvider: KeycloakSecurityMatcherProvider?,
) {
    private val keycloakRequestMatcher = keycloakRequestMatcherProvider?.invoke() ?: AnyRequestMatcher.INSTANCE
    private val keycloakSecurityMatcher = keycloakSecurityMatcherProvider?.invoke() ?: AnyServerWebExchangeMatcher

    /**
     * Configure filter as OAuth2 Client:
     * - redirect to interactive login if unauthenticated
     * - store access token in session
     *
     * If `spring-security-oauth2-client` is not on the classpath, or if [KeycloakProperties.bearerOnly] is set to
     * `true`, the filter will be disabled.
     */
    fun configureOAuth2Client(http: HttpSecurity) {
        // spring-security-oauth2-client is not on the classpath
        if (keycloakOAuth2ClientConfigurer == null) {
            disableFilter(http)
            return
        }

        http.requestMatcher(
            AndRequestMatcher(
                keycloakRequestMatcher,
                RedirectToInteractiveLoginRequestMatcher,
            ),
        )
        keycloakOAuth2ClientConfigurer.configureOAuth2Client(http)
    }

    /**
     * Configure filter as OAuth2 Client:
     * - redirect to interactive login if unauthenticated
     * - store access token in session
     *
     * If `spring-security-oauth2-client` is not on the classpath, or if [KeycloakProperties.bearerOnly] is set to
     * `true`, the filter will be disabled.
     */
    fun configureOAuth2Client(http: ServerHttpSecurity) {
        // spring-security-oauth2-client is not on the classpath
        if (keycloakReactiveOAuth2ClientConfigurer == null) {
            disableFilter(http)
            return
        }

        http.securityMatcher(
            AndServerWebExchangeMatcher(
                keycloakSecurityMatcher,
                RedirectToInteractiveLoginServerWebExchangeMatcher,
            ),
        )
        keycloakReactiveOAuth2ClientConfigurer.configureOAuth2Client(http)
    }

    /**
     * Configure filter as OAuth2 Resource Server:
     * - validate bearer token
     */
    fun configureOAuth2ResourceServer(http: HttpSecurity) {
        // spring-security-oauth2-resource-server is not on the classpath
        if (keycloakOAuth2ResourceServerConfigurer == null) {
            disableFilter(http)
            return
        }

        http.requestMatcher(keycloakRequestMatcher)
        keycloakOAuth2ResourceServerConfigurer.configureOAuth2ResourceServer(http)
        http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
    }

    /**
     * Configure filter as OAuth2 Resource Server:
     * - validate bearer token
     */
    fun configureOAuth2ResourceServer(http: ServerHttpSecurity) {
        // spring-security-oauth2-resource-server is not on the classpath
        if (keycloakReactiveOAuth2ResourceServerConfigurer == null) {
            disableFilter(http)
            return
        }

        http.securityMatcher(keycloakSecurityMatcher)
        keycloakReactiveOAuth2ResourceServerConfigurer.configureOAuth2ResourceServer(http)
    }

    private fun disableFilter(http: HttpSecurity) {
        http.requestMatcher(NegatedRequestMatcher(AnyRequestMatcher.INSTANCE))
    }

    private fun disableFilter(http: ServerHttpSecurity) {
        http.securityMatcher(NegatedServerWebExchangeMatcher(AnyServerWebExchangeMatcher))
    }
}
