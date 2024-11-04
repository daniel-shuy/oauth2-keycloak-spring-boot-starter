package com.github.daniel.shuy.oauth2.keycloak.config

import org.springframework.security.config.web.server.ServerHttpSecurity

public fun interface KeycloakReactiveWebSecurityConfigurerAdapter {
    public fun configure(http: ServerHttpSecurity)
}

public open class DefaultKeycloakReactiveWebSecurityConfigurerAdapter : KeycloakReactiveWebSecurityConfigurerAdapter {
    override fun configure(http: ServerHttpSecurity) {
        http.authorizeExchange { it.anyExchange().denyAll() }
    }
}
