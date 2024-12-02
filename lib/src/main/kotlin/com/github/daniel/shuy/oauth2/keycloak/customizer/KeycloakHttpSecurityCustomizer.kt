package com.github.daniel.shuy.oauth2.keycloak.customizer

import org.springframework.security.config.annotation.web.builders.HttpSecurity

public fun interface KeycloakHttpSecurityCustomizer {
    public fun configure(http: HttpSecurity)
}

public open class DefaultKeycloakHttpSecurityCustomizer : KeycloakHttpSecurityCustomizer {
    override fun configure(http: HttpSecurity) {
        http.authorizeHttpRequests { it.anyRequest().denyAll() }
    }
}
