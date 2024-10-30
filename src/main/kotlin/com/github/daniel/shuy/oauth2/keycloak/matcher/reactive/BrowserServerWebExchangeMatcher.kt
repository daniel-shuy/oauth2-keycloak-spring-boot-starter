package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * [ServerWebExchangeMatcher] that determines if a given request is from a web browser.
 */
internal object BrowserServerWebExchangeMatcher : ServerWebExchangeMatcher {
    /**
     * Returns [MatchResult.match] if the given request is from a web browser or [MatchResult.notMatch] otherwise.
     *
     * @param exchange the exchange to check for a match
     * @return [MatchResult.match] if the given request is from a web browser, [MatchResult.notMatch] otherwise
     */
    override fun matches(exchange: ServerWebExchange): Mono<MatchResult> =
        // most web browsers will include an Accept header that includes text/html
        if (
            exchange.request.headers[HttpHeaders.ACCEPT]
                .orEmpty()
                .flatMap(MediaType::parseMediaTypes)
                .any { it.includes(MediaType.TEXT_HTML) }
        ) {
            MatchResult.match()
        } else {
            MatchResult.notMatch()
        }
}
