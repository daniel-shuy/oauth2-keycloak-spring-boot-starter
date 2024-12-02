package com.github.daniel.shuy.oauth2.keycloak.matcher.servlet

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.web.util.matcher.RequestMatcher
import java.util.Enumeration
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * [RequestMatcher] that determines if a given request is from a web browser.
 */
internal object BrowserRequestMatcher : RequestMatcher {
    /**
     * Returns `true` if the given request is from a web browser or `false` otherwise.
     *
     * @param request the request to check for a match
     * @return `true` if the given request is from a web browser, `false` otherwise
     */
    override fun matches(request: HttpServletRequest): Boolean =
        // most web browsers will include an Accept header that includes text/html
        request
            .getHeaders(HttpHeaders.ACCEPT)
            .stream()
            .map(MediaType::parseMediaTypes)
            .flatMap { it.stream() }
            .anyMatch { it.includes(MediaType.TEXT_HTML) }

    private fun <T> Enumeration<T>.stream(): Stream<T> = StreamSupport.stream(spliterator(), false)

    private fun <T> Enumeration<T>.spliterator(): Spliterator<T> = Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED)
}
