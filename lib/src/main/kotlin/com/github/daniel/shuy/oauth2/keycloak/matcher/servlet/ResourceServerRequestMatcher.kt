package com.github.daniel.shuy.oauth2.keycloak.matcher.servlet

import org.springframework.http.HttpHeaders
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * [RequestMatcher] that determines if a given request should delegate to OAuth2 Resource Server.
 */
internal val ResourceServerRequestMatcher =
    OrRequestMatcher(
        // if request is not from a web browser
        NegatedRequestMatcher(BrowserRequestMatcher),
        // if request has bearer token
        RequestHeaderRequestMatcher(HttpHeaders.AUTHORIZATION),
    )
