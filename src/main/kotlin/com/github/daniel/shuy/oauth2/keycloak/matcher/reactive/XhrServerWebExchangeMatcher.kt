package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * [ServerWebExchangeMatcher] that determines if a given request is an XHR (`XMLHttpRequest`).
 */
internal object XhrServerWebExchangeMatcher : ServerWebExchangeMatcher {
    private const val X_REQUESTED_WITH_HEADER = "X-Requested-With"
    private const val X_REQUESTED_WITH_HEADER_AJAX_VALUE = "XMLHttpRequest"

    /**
     * Returns [MatchResult.match] if the given request is an XHR request or [MatchResult.notMatch] otherwise.
     *
     * @param exchange the exchange to check for a match
     * @return [MatchResult.match] if the given request is an AJAX request, [MatchResult.notMatch] otherwise
     */
    override fun matches(exchange: ServerWebExchange): Mono<MatchResult> =
        // most XHR libraries include an X-Requested-With header
        if (
            exchange.request.headers[X_REQUESTED_WITH_HEADER].orEmpty()
                .contains(X_REQUESTED_WITH_HEADER_AJAX_VALUE)
        ) {
            MatchResult.match()
        } else {
            MatchResult.notMatch()
        }
}
