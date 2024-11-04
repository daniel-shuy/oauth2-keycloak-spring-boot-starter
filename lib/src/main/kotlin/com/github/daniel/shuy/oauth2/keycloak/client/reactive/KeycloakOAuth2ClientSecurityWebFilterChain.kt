package com.github.daniel.shuy.oauth2.keycloak.client.reactive

import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

public class KeycloakOAuth2ClientSecurityWebFilterChain private constructor(delegatee: SecurityWebFilterChain) :
    SecurityWebFilterChain by delegatee {
        public constructor(http: ServerHttpSecurity) : this(http.build())
    }
