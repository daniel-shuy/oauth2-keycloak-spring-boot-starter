package com.github.daniel.shuy.oauth2.keycloak.server.resource.reactive

import com.github.daniel.shuy.oauth2.keycloak.config.KeycloakReactiveWebSecurityConfigurerAdapter
import com.github.daniel.shuy.oauth2.keycloak.matcher.reactive.ResourceServerServerWebExchangeMatcher
import com.github.daniel.shuy.oauth2.keycloak.server.resource.KeycloakJwtAuthenticationConverter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import reactor.core.publisher.Flux

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(ReactiveOAuth2ResourceServerAutoConfiguration::class)
@ConditionalOnClass(
    Flux::class,
    EnableWebFluxSecurity::class,
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
internal class KeycloakReactiveResourceServerConfiguration {
    @Bean
    fun keycloakReactiveJwtAuthenticationConverter(keycloakJwtAuthenticationConverter: KeycloakJwtAuthenticationConverter) =
        KeycloakReactiveJwtAuthenticationConverter(keycloakJwtAuthenticationConverter)

    @Bean
    @ConditionalOnMissingBean
    fun keycloakReactiveOAuth2ResourceServerConfigurer(
        keycloakJwtAuthenticationConverter: KeycloakReactiveJwtAuthenticationConverter,
    ): KeycloakReactiveOAuth2ResourceServerConfigurer =
        DefaultKeycloakReactiveOAuth2ResourceServerConfigurer(
            keycloakJwtAuthenticationConverter,
        )

    @Bean
    @ConditionalOnMissingBean
    fun keycloakOAuth2ResourceServerSecurityWebFilterChain(
        http: ServerHttpSecurity,
        keycloakReactiveWebSecurityConfigurerAdapter: KeycloakReactiveWebSecurityConfigurerAdapter,
        keycloakReactiveOAuth2ResourceServerConfigurer: KeycloakReactiveOAuth2ResourceServerConfigurer,
    ): KeycloakOAuth2ResourceServerSecurityWebFilterChain {
        keycloakReactiveWebSecurityConfigurerAdapter.configure(http)
        keycloakReactiveOAuth2ResourceServerConfigurer.configureOAuth2ResourceServer(http)
        return KeycloakOAuth2ResourceServerSecurityWebFilterChain(http)
    }

    @Bean
    fun keycloakOAuth2ResourceServerServerWebExchangeMatcher() = ResourceServerServerWebExchangeMatcher()
}
