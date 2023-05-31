package com.github.daniel.shuy.oauth2.keycloak.servlet

import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * Provider for [RequestMatcher] that determines if a given request is protected by Keycloak.
 */
interface KeycloakRequestMatcherProvider : () -> RequestMatcher
