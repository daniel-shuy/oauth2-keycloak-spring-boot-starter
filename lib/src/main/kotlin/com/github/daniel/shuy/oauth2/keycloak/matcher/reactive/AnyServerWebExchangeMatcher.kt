package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Matches any supplied request.
 */
internal object AnyServerWebExchangeMatcher : ServerWebExchangeMatcher {
    override fun matches(exchange: ServerWebExchange?): Mono<MatchResult> = MatchResult.match()
}
