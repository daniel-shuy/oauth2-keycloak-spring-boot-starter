package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.http.HttpHeaders
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher

/**
 * [ServerWebExchangeMatcher] that determines if a given request should redirect to interactive login page.
 */
internal val RedirectToInteractiveLoginServerWebExchangeMatcher: ServerWebExchangeMatcher = AndServerWebExchangeMatcher(
    // don't redirect to interactive login page if request is an XHR
    NegatedServerWebExchangeMatcher(XhrServerWebExchangeMatcher),
    // don't redirect to interactive login page if already logged in
    NegatedServerWebExchangeMatcher(RequestHeaderServerWebExchangeMatcher(HttpHeaders.AUTHORIZATION)),
)
