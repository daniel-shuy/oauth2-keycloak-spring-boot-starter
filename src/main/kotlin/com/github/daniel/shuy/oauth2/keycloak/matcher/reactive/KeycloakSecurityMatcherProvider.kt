package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher

/**
 * Provider for [ServerWebExchangeMatcher] that determines if a given request is protected by Keycloak.
 */
interface KeycloakSecurityMatcherProvider : () -> ServerWebExchangeMatcher
