package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.http.HttpHeaders
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher

/**
 * [ServerWebExchangeMatcher] that determines if a given request should be handled by OAuth2 Resource Server.
 */
internal val ResourceServerServerWebExchangeMatcher =
    OrServerWebExchangeMatcher(
        // if request is an XHR
        XhrServerWebExchangeMatcher,
        // if request has bearer token
        RequestHeaderServerWebExchangeMatcher(HttpHeaders.AUTHORIZATION),
    )
