package com.github.daniel.shuy.oauth2.keycloak.reactive.server.resource

import com.github.daniel.shuy.oauth2.keycloak.converter.KeycloakJwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter

/**
 * Adapts the given [KeycloakJwtAuthenticationConverter] for reactive.
 */
class KeycloakReactiveJwtAuthenticationConverter(
    keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter,
) : ReactiveJwtAuthenticationConverterAdapter(keycloakJwtAuthenticationConverter)
