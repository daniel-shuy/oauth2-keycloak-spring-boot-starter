package com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive

import com.github.daniel.shuy.oauth2.keycloak.server.resource.KeycloakJwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter

/**
 * Adapts the given [KeycloakJwtAuthenticationConverter] for reactive.
 */
class KeycloakReactiveJwtAuthenticationConverter(
    keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
) : ReactiveJwtAuthenticationConverterAdapter(keycloakJwtAuthenticationConverter)
