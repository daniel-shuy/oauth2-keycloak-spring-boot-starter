package com.github.daniel.shuy.oauth2.keycloak.matcher.servlet

import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * Provider for [RequestMatcher] that determines if a given request is protected by Keycloak.
 */
public interface KeycloakRequestMatcherProvider : () -> RequestMatcher
