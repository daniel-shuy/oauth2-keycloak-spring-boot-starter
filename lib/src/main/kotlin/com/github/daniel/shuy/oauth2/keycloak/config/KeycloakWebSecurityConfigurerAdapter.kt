package com.github.daniel.shuy.oauth2.keycloak.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity

public fun interface KeycloakWebSecurityConfigurerAdapter {
    public fun configure(http: HttpSecurity)
}

public open class DefaultKeycloakWebSecurityConfigurerAdapter : KeycloakWebSecurityConfigurerAdapter {
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests { it.anyRequest().denyAll() }
    }
}
