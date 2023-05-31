package com.github.daniel.shuy.oauth2.keycloak.servlet.matcher

import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.http.HttpServletRequest

/**
 * [RequestMatcher] that determines if a given request is an XHR (XMLHttpRequest) or an interactive login request.
 */
internal object XhrRequestMatcher : RequestMatcher {
    private const val X_REQUESTED_WITH_HEADER = "X-Requested-With"
    private const val X_REQUESTED_WITH_HEADER_AJAX_VALUE = "XMLHttpRequest"

    /**
     * Returns `true` if the given request is an API request or `false` otherwise.
     *
     * @param request the request to check for a match
     * @return `true` if the given request is an AJAX request, `false` otherwise
     */
    override fun matches(request: HttpServletRequest): Boolean =
        // most AJAX libraries include an X-Requested-With header
        X_REQUESTED_WITH_HEADER_AJAX_VALUE == request.getHeader(X_REQUESTED_WITH_HEADER)
}
