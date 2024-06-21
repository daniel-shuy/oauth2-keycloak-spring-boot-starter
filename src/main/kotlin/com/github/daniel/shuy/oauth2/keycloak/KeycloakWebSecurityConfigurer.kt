package com.github.daniel.shuy.oauth2.keycloak

import com.github.daniel.shuy.oauth2.keycloak.client.reactive.KeycloakReactiveOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.client.servlet.KeycloakOAuth2ClientConfigurer
import com.github.daniel.shuy.oauth2.keycloak.matcher.reactive.AnyServerWebExchangeMatcher
import com.github.daniel.shuy.oauth2.keycloak.matcher.reactive.ResourceServerServerWebExchangeMatcher
import com.github.daniel.shuy.oauth2.keycloak.matcher.servlet.ResourceServerRequestMatcher
import com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive.KeycloakReactiveOAuth2ResourceServerConfigurer
import com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet.KeycloakOAuth2ResourceServerConfigurer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher

/**
 * Utility class to configure web security for Keycloak.
 */
public class KeycloakWebSecurityConfigurer(
    private val keycloakOAuth2ClientConfigurer: KeycloakOAuth2ClientConfigurer?,
    private val keycloakReactiveOAuth2ClientConfigurer: KeycloakReactiveOAuth2ClientConfigurer?,
    private val keycloakOAuth2ResourceServerConfigurer: KeycloakOAuth2ResourceServerConfigurer?,
    private val keycloakReactiveOAuth2ResourceServerConfigurer: KeycloakReactiveOAuth2ResourceServerConfigurer?,
) {

    /**
     * Configure filter as OAuth2 Client:
     * - redirect to interactive login if unauthenticated
     * - store access token in session
     *
     * If `spring-security-oauth2-client` is not on the classpath, or if [KeycloakProperties.bearerOnly] is set to
     * `true`, the filter will be disabled.
     */
    public fun configureOAuth2Client(http: HttpSecurity) {
        // spring-security-oauth2-client is not on the classpath
        if (keycloakOAuth2ClientConfigurer == null) {
            disableFilter(http)
            return
        }

        http.requestMatcher(NegatedRequestMatcher(ResourceServerRequestMatcher))
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
    public fun configureOAuth2Client(http: ServerHttpSecurity) {
        // spring-security-oauth2-client is not on the classpath
        if (keycloakReactiveOAuth2ClientConfigurer == null) {
            disableFilter(http)
            return
        }

        http.securityMatcher(NegatedServerWebExchangeMatcher(ResourceServerServerWebExchangeMatcher))
        keycloakReactiveOAuth2ClientConfigurer.configureOAuth2Client(http)
    }

    /**
     * Configure filter as OAuth2 Resource Server:
     * - validate bearer token
     */
    public fun configureOAuth2ResourceServer(http: HttpSecurity) {
        // spring-security-oauth2-resource-server is not on the classpath
        if (keycloakOAuth2ResourceServerConfigurer == null) {
            disableFilter(http)
            return
        }

        keycloakOAuth2ResourceServerConfigurer.configureOAuth2ResourceServer(http)
        http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
    }

    /**
     * Configure filter as OAuth2 Resource Server:
     * - validate bearer token
     */
    public fun configureOAuth2ResourceServer(http: ServerHttpSecurity) {
        // spring-security-oauth2-resource-server is not on the classpath
        if (keycloakReactiveOAuth2ResourceServerConfigurer == null) {
            disableFilter(http)
            return
        }

        keycloakReactiveOAuth2ResourceServerConfigurer.configureOAuth2ResourceServer(http)
    }

    private fun disableFilter(http: HttpSecurity) {
        http.requestMatcher(NegatedRequestMatcher(AnyRequestMatcher.INSTANCE))
    }

    private fun disableFilter(http: ServerHttpSecurity) {
        http.securityMatcher(NegatedServerWebExchangeMatcher(AnyServerWebExchangeMatcher))
    }
}
