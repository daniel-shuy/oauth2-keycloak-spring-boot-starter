package com.github.daniel.shuy.oauth2.keycloak.matcher.reactive

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * A [ServerWebExchangeMatcher] that can be used to match request that contain a header with an expected header name and
 * an expected value.
 *
 * For example, the following will match a request that contains a header with the name "X-Requested-With" no matter
 * what the value is.
 * ```
 * ServerWebExchangeMatcher matcher = new RequestHeaderServerWebExchangeMatcher("X-Requested-With");
 * ```
 *
 * Alternatively, the [RequestHeaderServerWebExchangeMatcher] can be more precise and require a specific value. For
 * example the following will match on requests with the header name of "X-Requested-With" with the value of
 * "XMLHttpRequest", but will not match on header name of "X-Requested-With" with the value of "Other".
 * ```
 * ServerWebExchangeMatcher matcher = new RequestHeaderServerWebExchangeMatcher("X-Requested-With", "XMLHttpRequest");
 * ```
 *
 * If there are multiple header values, then it will match if any of them matches the specific value. So in the previous
 * example if the header "X-Requested-With" contains the values "Other" and "XMLHttpRequest", then it will match.
 *
 * @constructor Creates a new instance that will match if a header by the name of [expectedHeaderName] is present and if
 * the [expectedHeaderValue] is non-null the first value is the same.
 *
 * @param expectedHeaderName the name of the expected header that if present the request will match. Cannot be `null`.
 * @param expectedHeaderValue the expected header value or `null` if the value does not matter
 *
 * @see RequestHeaderRequestMatcher
 */
internal class RequestHeaderServerWebExchangeMatcher @JvmOverloads constructor(
    private val expectedHeaderName: String,
    private val expectedHeaderValue: String? = null,
) : ServerWebExchangeMatcher {
    override fun matches(exchange: ServerWebExchange): Mono<MatchResult> {
        val headerValues = exchange.request.headers[expectedHeaderName]
            ?: return MatchResult.notMatch()

        if (expectedHeaderValue == null) {
            return MatchResult.match()
        }

        return if (headerValues.contains(expectedHeaderValue)) MatchResult.match() else MatchResult.notMatch()
    }

    override fun toString(): String =
        "${this::class.simpleName} [${::expectedHeaderName.name}=$expectedHeaderName, ${::expectedHeaderValue}=$expectedHeaderValue]"
}
