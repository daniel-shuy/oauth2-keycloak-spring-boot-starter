package com.github.daniel.shuy.oauth2.keycloak.servlet.matcher

import org.springframework.http.HttpHeaders
import org.springframework.security.web.util.matcher.AndRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * [RequestMatcher] that determines if a given request should redirect to interactive login page.
 */
internal val RedirectToInteractiveLoginRequestMatcher: RequestMatcher = AndRequestMatcher(
    // don't redirect to interactive login page if request is an XHR
    NegatedRequestMatcher(XhrRequestMatcher),
    // don't redirect to interactive login page if already logged in
    NegatedRequestMatcher(RequestHeaderRequestMatcher(HttpHeaders.AUTHORIZATION)),
)
