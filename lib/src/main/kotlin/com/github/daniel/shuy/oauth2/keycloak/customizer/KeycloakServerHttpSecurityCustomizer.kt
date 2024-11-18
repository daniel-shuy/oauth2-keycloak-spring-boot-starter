package com.github.daniel.shuy.oauth2.keycloak.customizer

import org.springframework.security.config.web.server.ServerHttpSecurity

public fun interface KeycloakServerHttpSecurityCustomizer {
    public fun configure(http: ServerHttpSecurity)
}

public open class DefaultKeycloakServerHttpSecurityCustomizer : KeycloakServerHttpSecurityCustomizer {
    override fun configure(http: ServerHttpSecurity) {
        http.authorizeExchange { it.anyExchange().denyAll() }
    }
}
