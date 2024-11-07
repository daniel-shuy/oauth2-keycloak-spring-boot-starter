package com.github.daniel.shuy.oauth2.keycloak.server.resource.servlet

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

public class KeycloakOAuth2ResourceServerSecurityFilterChain private constructor(delegatee: SecurityFilterChain) :
    SecurityFilterChain by delegatee {
        public constructor(http: HttpSecurity) : this(http.build())
    }
